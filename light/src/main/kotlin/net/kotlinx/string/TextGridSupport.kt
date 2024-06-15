package net.kotlinx.string

import net.kotlinx.json.gson.GsonData
import net.kotlinx.reflect.Bean

//==================================================== 리플렉션 때문에 이 프로젝트로 이동 ======================================================

/** Gson array 인 경우 -> 아테나 등 간단 출력시 활용 */
fun GsonData.print(limitSize: Int = TextGrid.TEXT_ABBR_LIMIT) {
    check(this.delegate.isJsonArray)
    if (this.empty) {
        return  //별도 로그나 예외처리 하지않음
    }
    this.map { it }.print(limitSize)
}

/**
 * List 의 경우 다양한 케이스가 있을 수 있음 주의!
 * reified임으로 향후 길어지면 나누자.
 * */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> List<T>.print(limitSize: Int = TextGrid.TEXT_ABBR_LIMIT) {
    val list = this
    when (T::class) {

        /** List 안에 다시 List가 있는 경우 */
        List::class -> {
            val real = list as List<List<String>>
            val headers = real[0]
            val datas = real.drop(1).map { v -> v.map { it.abbr(limitSize, "..") }.toTypedArray() }
            headers.toTextGrid(datas).print()
        }

        Bean::class -> {
            val real = list as List<Bean>
            if (real.isEmpty()) return //비어있을때 예외처리하지 않음
            val header = real.first().toHeader()
            header.toTextGrid(real.map { it.toArray() }).print()
        }

        GsonData::class -> {
            val real = list as List<GsonData>
            if (real.isEmpty()) return //비어있을때 예외처리하지 않음

            val headers = real[0].entryMap().keys.toList()
            val datas = real.map { it.entryMap().values.map { g -> g.str?.abbr(limitSize, "..") }.toTypedArray() }
            headers.toTextGrid(datas).print()
        }

        /**
         * 일반적인 List<Domain> 인 경우 리플렉션으로 강제 출력
         *  */
        else -> {
            //주의!! 실 업무에 이렇게 쓰면 위험함.
            val real = list.map { Bean(it as Any) }
            if (real.isEmpty()) return //비어있을때 예외처리하지 않음
            val header = real.first().toHeader()
            header.toTextGrid(real.map { it.toArray() }).print()
        }
    }

}