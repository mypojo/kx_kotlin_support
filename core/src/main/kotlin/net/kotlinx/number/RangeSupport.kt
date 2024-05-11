package net.kotlinx.number

/**
 * 구간 수를 출력함
 * ex) 9 .. 23 = 15
 *  */
val IntRange.size: Int
    get() = this.toList().size

/**
 * 구간 수를 출력함
 * ex) 9 .. 23 = 15
 *  */
val LongRange.size: Int
    get() = this.toList().size

/**
 * 결과를 다시 분리해서 리턴함
 * 3/7  -> 전체를 7등분 해서 3번째만 리턴
 *  */
fun LongRange.split(total: Number, index: Number): LongRange {
    val range = this
    val subSpoliter = RangeSpliter {
        minmax = range
        stepCnt = total.toLong()
    }
    return subSpoliter[index.toInt()]
}