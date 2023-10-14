package net.kotlinx.core.collection


fun <T> Map<T, Long>.toMapNum() = MapNum(this)

/**
 * 정산 시스템 등에 사용되는 불변 map 계산기
 * map 내부의 숫자 데이터를 더하거나 빼줌.
 * ex) reduce { acc, mapNum -> acc + mapNum }
 * */
class MapNum<T>(
    private val delegate: Map<T, Long> = emptyMap()
) : Map<T, Long> by delegate {

    operator fun plus(append: MapNum<T>): MapNum<T> = doPlus(append, 1)
    operator fun minus(append: MapNum<T>): MapNum<T> = doPlus(append, -1)

    /** 두개의 데이터 값을 더한다. */
    private fun doPlus(append: MapNum<T>, arrow: Int): MapNum<T> {
        return MapNum(
            mapOf(
                *delegate.pairs(),
                *append.delegate.pairs().map {
                    it.first to ((delegate[it.first] ?: 0L) + it.second * arrow)
                }.toTypedArray(),
            )
        )
    }

    /** 간단 합꼐 */
    fun sum(): Long = delegate.values.sum()
}