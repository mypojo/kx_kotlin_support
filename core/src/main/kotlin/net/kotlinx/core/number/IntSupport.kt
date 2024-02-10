package net.kotlinx.core.number

/** 소수인지. */
inline fun Int.isPrimeNum(): Boolean {
    val num = this
    for (i in 2..num / 2) {
        // condition for nonprime number
        if (num % i == 0) {
            return true
        }
    }
    return false
}

/** coerceAtLeast 단어가 너무 헷갈려서 재정의함 */
fun Int.maxWith(compare: Int): Int = this.coerceAtLeast(compare)

/** coerceAtLeast 단어가 너무 헷갈려서 재정의함 */
fun Int.minWith(compare: Int): Int = this.coerceAtMost(compare)