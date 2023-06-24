package net.kotlinx.core.number


/** 문자와 숫자 간의 간단 변환 지원 */
object StringIntUtil {


    /**
     * 소문자로 바꿔준다~ 체크는 안함 귀찮..
     * 97 ~ 122 : a~z --> 0부터 시작
     * 65 ~ 90   : A ~ Z
     * */
    fun intToAlpha(i: Int): String {
        return String(charArrayOf((i + 97).toChar()))
    }

    /** 1부터 시작하는 int를 소문자 알파벳으로 바꿔준다. 귀찮아서 벨리체크 안함
     * 1-> a   */
    fun intToLowerAlpha(i: Int): String {
        return String(charArrayOf((i + 96).toChar()))
    }

    /**
     * 1부터 시작하는 int를 대문자 알파벳으로 바꿔준다. 귀찮아서 벨리체크 안함
     * 1-> A
     * 엑셀에서 위치 정할때 사용됨
     * */
    fun intToUpperAlpha(i: Int): String {
        return String(charArrayOf((i + 64).toChar()))
    }


    fun upperAlphaToInt(i: Char): Int {
        return i.code - 64
    }

}