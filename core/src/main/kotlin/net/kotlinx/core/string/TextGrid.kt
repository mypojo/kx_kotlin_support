package net.kotlinx.core.string

import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.time.toKr01
import java.time.LocalDateTime

/**
 * 헤더 기준으로 그리드 출력
 * 체인에 조금 부적합해 보이더라도 헤더가 먼저 오는게 적합해 보인다.
 *  */
inline fun List<String>.toTextGrid(datas: List<Array<out Any?>>) = TextGrid(this, datas)

/** 아테나 등 간단 출력시 활용 */
fun GsonData.print() {
    check(this.delegate.isJsonArray)
    if (this.empty) return //비어있을때 예외처리하지 않음
    val first = this[0].entryMap()
    val datas = this.map { it.entryMap().values.map { g -> g.str }.toTypedArray() }
    first.keys.toList().toTextGrid(datas).print()
}

inline fun <reified T> List<T>.print() {
    val list = this
    when (T::class) {
        List::class -> {
            val real = list as List<List<String>>
            real[0].toTextGrid(real.drop(1).map { it.toTypedArray() }).print()
        }

        GsonData::class -> {
            val real = list as List<GsonData>
            if (real.isEmpty()) return //비어있을때 예외처리하지 않음
            val first = real[0].entryMap()
            val datas = real.map { it.entryMap().values.map { g -> g.str }.toTypedArray() }
            first.keys.toList().toTextGrid(datas).print()
        }

        else -> throw IllegalArgumentException("${T::class} is not required")
    }

}


/**
 *
 * j-text-util 이 한글 깨지고, 텍스트 리턴이 안되거 걍 새로 만들었다.
 * https://github.com/naver/d2codingfont  <-- 고정폭 폰트 필수
 */
class TextGrid(
    private val headers: List<String>,
    private var datas: List<Array<out Any?>>,
    private val lineSeparator: String = "\n",
) {

    enum class TextGridType {
        TEXT, NUM, ;
    }

    /** 개행 제거해줌 */
    private val lineSeparatorReg = lineSeparator.toRegex()

    /** 실제 출력물 텍스트 */
    val text: String by lazy {

        val convertedDatas = datas.map { array ->
            array.map {
                when (it) {
                    is Number -> it.toString() //닷 붙이기
                    is LocalDateTime -> it.toKr01()
                    is Collection<*> -> it.joinToString(",")
                    else -> it.toString().removeFrom(lineSeparatorReg)
                }
            }.take(headers.size) //헤더보다 길면 제거
        }
        //헤더 포함 세로 베이스로 가장 큰 숫자를 구함
        val maxOfGridColumns = listOf(headers, *convertedDatas.toTypedArray()).let { datas ->
            headers.indices.associateWith { i -> datas.map { it[i] }.maxOf { it.space() } }
        }
        //전체 크기 구함
        val gridFullSize: Int = maxOfGridColumns.values.sum() + maxOfGridColumns.size * 3 + 1

        //컬럼별 타입을 지정해줌
        val columnType = datas.firstOrNull()?.mapIndexed { index, any ->
            index to when (any) {
                is Number -> TextGridType.NUM
                else -> TextGridType.TEXT
            }
        }?.toMap() ?: emptyMap()

        fun resizeAndInline(input: List<out String>): String {
            val resized = input.mapIndexed { index, org ->
                val size: Int = org.space()
                val maxSize: Int = maxOfGridColumns[index]!!
                val interval = (maxSize - size).coerceAtLeast(0)
                // 문자는 촤측 정렬, 숫자는 우측 정렬
                when (columnType[index]) {
                    TextGridType.NUM -> " ".repeat(interval) + org
                    else -> org + " ".repeat(interval)
                }
            }
            return "| ${resized.joinToString(" | ")} |"
        }

        val body = if (datas.isEmpty()) {
            listOf("|${" ".repeat(gridFullSize - 19)}데이터가 없습니다|")
        } else {
            convertedDatas.map { resizeAndInline(it) }
        }
        arrayOf(
            "_".repeat(gridFullSize),
            resizeAndInline(headers), //헤더
            "|${"=".repeat(gridFullSize - 2)}|",
            *body.toTypedArray(),//본문
            " ".repeat(gridFullSize),//푸터
        ).joinToString(lineSeparator)
    }

    /** 인라인용 출력기. 테스트 등에 사용 slf4j를 쓰게되면 첫 라인이 버려져서 이렇게 함.  */
    fun print() = println(text)

}