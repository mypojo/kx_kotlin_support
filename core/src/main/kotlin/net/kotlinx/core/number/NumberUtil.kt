package net.kotlinx.core.number


/** 문자와 숫자 간의 간단 변환 지원 */
object NumberUtil {

    /**
     * 숫자(0부터 시작)가 차지하는 사이즈를 리턴한다.
     * ex) 1000 개 숫자는 000 ~ 999 -> 3자리 필요
     * ex) 1001개 숫자는 0000 ~ 10000 -> 4자리 필요
     * */
    fun numPadSize(max: Int): Int {
        return (max - 1).toString().length
    }


}