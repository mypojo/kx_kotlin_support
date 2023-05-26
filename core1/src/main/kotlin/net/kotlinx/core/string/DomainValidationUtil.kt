package net.kotlinx.core.string

import net.kotlinx.core.regex.RegexSet

/**
 * 도메인 벨리데이션 관련 코드
 */
object DomainValidationUtil {
    /**
     * 사업자 등록번호인지 체크한다.
     */
    fun isBusinessId(str: String): Boolean {
        val strs = str.split("".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.size != 10) return false

        val ints = IntArray(10)
        for (i in 0..9) {
            ints[i] = strs[i].toInt()
        }
        var sum = 0
        val indexs = intArrayOf(1, 3, 7, 1, 3, 7, 1, 3)
        for (i in 0..7) {
            sum += ints[i] * indexs[i]
        }
        val num = ints[8] * 5
        sum += num / 10 + num % 10
        sum = 10 - sum % 10
        return if (sum == 10) {
            0 == ints[9]
        } else {
            sum == ints[9]
        }
    }

    /**
     * 주민등록번호인지 체크한다. 1. 주민등록번호의 앞 6자리의 수에 처음부터 차례대로 2,3,4,5,6,7 을 곱한다. 그 다음, 뒤
     * 7자리의 수에 마지막 자리만 제외하고 차례대로 8,9,2,3,4,5 를 곱한다. 2. 이렇게 곱한 각 자리의 수들을 모두 더한다.
     * 3. 모두 더한 수를 11로 나눈 나머지를 구한다. 4. 이 나머지를 11에서 뺀다. 5. 이렇게 해서 나온 최종 값을
     * 주민등록번호의 마지막 자리 수와 비교해서 같으면 유효한 번호이고 다르면 잘못된 값이다.
     */
    fun isSid(input: String): Boolean {
        val sid: String = input.retainFrom(RegexSet.NUMERIC)
        check(sid.length == 13) { "주민등록번호 자리수 13자리를 확인하기 바랍니다." }

        // 입력받은 주민번호 앞자리 유효성 검증============================
        val leftSid = sid.substring(0, 6)
        val rightSid = sid.substring(6, 13)
        val yy = leftSid.substring(0, 2).toInt()
        val mm = leftSid.substring(2, 4).toInt()
        val dd = leftSid.substring(4, 6).toInt()
        if (yy < 1 || yy > 99 || mm > 12 || mm < 1 || dd < 1 || dd > 31) return false
        val digit1 = leftSid.substring(0, 1).toInt() * 2
        val digit2 = leftSid.substring(1, 2).toInt() * 3
        val digit3 = leftSid.substring(2, 3).toInt() * 4
        val digit4 = leftSid.substring(3, 4).toInt() * 5
        val digit5 = leftSid.substring(4, 5).toInt() * 6
        val digit6 = leftSid.substring(5, 6).toInt() * 7
        val digit7 = rightSid.substring(0, 1).toInt() * 8
        val digit8 = rightSid.substring(1, 2).toInt() * 9
        val digit9 = rightSid.substring(2, 3).toInt() * 2
        val digit10 = rightSid.substring(3, 4).toInt() * 3
        val digit11 = rightSid.substring(4, 5).toInt() * 4
        val digit12 = rightSid.substring(5, 6).toInt() * 5
        val lastDigit = rightSid.substring(6, 7).toInt()
        val errorVerify = (digit1 + digit2 + digit3 + digit4 + digit5 + digit6 + digit7 + digit8 + digit9 + digit10 + digit11 + digit12) % 11
        val sumDigit: Int = if (errorVerify == 0) {
            1
        } else {
            11 - errorVerify
        }
        return lastDigit == sumDigit
    }
}
