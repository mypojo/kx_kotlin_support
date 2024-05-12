package net.kotlinx.string

/**
 * 자주 사용되지 않는 간이 도구용.
 * 자주 사용되는거는 *Support 참고
 * */
object CharUtil {

    /**
     * 문자열이 잘못된 인코딩으로 적용되었는지 간이 체크
     * � 문자가 포함되었는지 여부로 체크한다.
     *  */
    fun isValid(text: String): Boolean = !text.contains('\uFFFD')

}