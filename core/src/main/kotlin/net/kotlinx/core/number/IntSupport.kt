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