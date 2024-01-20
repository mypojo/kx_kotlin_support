package net.kotlinx.core.number

/**
 * 구간 수를 출력함
 * ex) 9 .. 23 = 15
 *  */
val IntRange.size: Int
    get() = this.last - this.first + 1

/**
 * 구간 수를 출력함
 * ex) 9 .. 23 = 15
 *  */
val LongRange.size: Long
    get() = this.last - this.first + 1