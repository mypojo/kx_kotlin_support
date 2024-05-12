package net.kotlinx.string

import mu.KotlinLogging
import net.kotlinx.number.maxWith
import net.kotlinx.time.toKr01
import java.time.LocalDateTime

/**
 * 헤더 기준으로 그리드 출력
 * 체인에 조금 부적합해 보이더라도 헤더가 먼저 오는게 적합해 보인다.
 *  */
fun List<String>.toTextGrid(datas: List<Array<out Any?>>) = TextGrid(this, datas)

/**
 * 즉시 print함
 * */
inline fun List<String>.toTextGridPrint(block: () -> List<Array<out Any?>>) = TextGrid(this, block()).print()

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
        TEXT, NUM,
        ;
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

        fun resizeAndInline(input: List<String>): String {
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
            val emptySize = (gridFullSize - EMPTY_MSG_SPACE - 2).maxWith(0) //좌우 | 2칸 제외
            log.trace { "gridFullSize $gridFullSize  -> repeat $emptySize" }
            listOf("|${" ".repeat(emptySize)}${EMPTY_MSG}|")
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

    companion object {

        /** 이 사이즈가 넘으면 보통 comsole에서 의미없다고 생각하고 줄임 */
        const val TEXT_ABBR_LIMIT = 40

        private val log = KotlinLogging.logger {}

        /** 값이 없을때 보여줄 메세지 */
        const val EMPTY_MSG = "데이터 없음"

        /** 빈 메세지 사이즈 */
        val EMPTY_MSG_SPACE = EMPTY_MSG.space()

    }

}