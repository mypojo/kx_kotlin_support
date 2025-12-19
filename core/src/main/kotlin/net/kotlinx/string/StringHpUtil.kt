package net.kotlinx.string

import net.kotlinx.core.VibeCoding

/**
 * 한국 휴대폰 번호 유틸리티
 * - 국내형(010-####-#### 등) ↔ 국제형(E.164, +8210########)
 * - 규칙
 *   - 국내형 → 국제형: 선행 0 제거 후 +82 접두 부여 (예: 010-1111-2222 → +821011112222)
 *   - 국제형 → 국내형: +82 제거 후 선행 0 복원, 하이픈 포맷 010-####-#### 적용
 * - 제약
 *   - 국제형 입력은 반드시 '+' 포함 (예: '+821011112222')
 *   - 옛 식별자(011/016/017/018/019) 미지원, 오직 010만 허용
 */
@VibeCoding
object StringHpUtil {

    /**
     * 국내형(010-####-#### 등) → 국제형(E.164, +82)
     * @throws IllegalArgumentException 잘못된 입력 형식일 때
     */
    fun toE164Kr(input: String): String {
        val digits = input.filter { it.isDigit() }
        require(digits.length == 11 && digits.startsWith("010")) {
            "국내형 번호는 010으로 시작하는 11자리여야 합니다: $input"
        }
        val withoutLeadingZero = digits.drop(1) // 0 제거 → 10########
        return "+82$withoutLeadingZero"
    }

    /**
     * 국제형(E.164, +8210########) → 국내형(010-####-####)
     * @throws IllegalArgumentException 잘못된 입력 형식일 때
     */
    fun fromE164Kr(e164: String): String {
        val normalized = e164.filterIndexed { index, c ->
            when {
                c.isDigit() -> true
                c == '+' && index == 0 -> true
                else -> false
            }
        }

        require(normalized.startsWith("+82")) { "국제형 번호는 +82로 시작해야 합니다: $e164" }

        val nationalWithoutZero = normalized.removePrefix("+82")
        require(nationalWithoutZero.length == 10 && nationalWithoutZero.startsWith("10")) {
            "국제형 번호는 +8210 으로 시작하고 총 12자(+ 포함 제외시 10자리)여야 합니다: $e164"
        }

        val national = "0$nationalWithoutZero" // 010########
        return formatDomestic010(national)
    }

    /** 010 11자리를 010-####-#### 포맷으로 변환 */
    private fun formatDomestic010(digits11: String): String {
        require(digits11.length == 11 && digits11.startsWith("010")) {
            "국내 포맷팅은 010으로 시작하는 11자리만 지원합니다: $digits11"
        }
        return buildString(13) {
            append(digits11.substring(0, 3))
            append('-')
            append(digits11.substring(3, 7))
            append('-')
            append(digits11.substring(7))
        }
    }
}
