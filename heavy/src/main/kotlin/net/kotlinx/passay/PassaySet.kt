package net.kotlinx.passay

import org.passay.*


/**
 * 간단 규칙 모움
 * https://www.baeldung.com/java-passay
 * */
object PassaySet {

    /** 대소문자+숫자+기본특문 */
    val BASIC_CHARS = listOf(
        ('a'..'z'),
        ('A'..'Z'),
        ('0'..'9'),
        listOf('!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~')
    ).flatten()

    /**
     * 기본 벨리데이터
     * 개인정보의 기술적·관리적 보호조치 기준 제4조(접근통제) ⑦ 정보통신서비스 제공자등은 이용자가 안전한 비밀번호를 이용할 수 있도록 비밀번호 작성규칙을 수립하고, 이행한다.
     *   1. 영문, 숫자, 특수문자 중 2종류 이상을 조합하여 최소 10자리 이상 또는 3종류 이상을 조합하여 최소 8자리 이상의 길이로 구성
     *   2. 연속적인 숫자나 생일, 전화번호 등 추측하기 쉬운 개인정보 및 아이디와 비슷한 비밀번호는 사용하지 않는 것을 권고
     *   3. 비밀번호에 유효기간을 설정하여 반기별 1회 이상 변경
     * */
    val BASIC: PasswordValidator by lazy {
        PasswordValidator(
            /** 기본 문자열 */
            AllowedCharacterRule(BASIC_CHARS.toCharArray()),
            /** 최소 최대 길이 */
            LengthRule(10, 20),
            /** X자 이상 반목되는 문자 사용금지 */
            RepeatCharacterRegexRule(3),
            /** 대/소/특/숫 이렇게 4가지 중 3가지 이상 조합 필수 */
            CharacterCharacteristicsRule(
                3,
                CharacterRule(EnglishCharacterData.LowerCase, 1),
                CharacterRule(EnglishCharacterData.UpperCase, 1),
                CharacterRule(EnglishCharacterData.Digit),
                CharacterRule(EnglishCharacterData.Special)
            )
        )
    }

}
