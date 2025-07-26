package net.kotlinx.time

/** 가능하면 measureTime을 사용하자 */
data class TimeStart(
    private val start: Long = System.currentTimeMillis(),
) {

    /** 지금 기준 시작과의  차이 */
    fun interval(): Long = System.currentTimeMillis() - start

    override fun toString(): String {
        return TimeString(interval()).toString()
    }
}