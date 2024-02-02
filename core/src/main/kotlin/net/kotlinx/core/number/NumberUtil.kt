package net.kotlinx.core.number


/** 문자와 숫자 간의 간단 변환 지원 */
object NumberUtil {

    /** 숫자(0부터 시작)가 차지하는 사이즈를 리턴한다. ex) 1000 -> 3 , 1001 -> 4, 99 -> 2  */
    fun numPadSize(max: Int): Int {
        return (max - 1).toString().length
    }


}