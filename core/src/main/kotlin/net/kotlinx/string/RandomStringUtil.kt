package net.kotlinx.string

import java.security.SecureRandom
import kotlin.random.Random

object RandomStringUtil {

    /**
     * n개의 랜덤 문자열을 가져온다. 커먼즈 넣기 싫어서 만들었음
     * RandomStringUtils.randomAlphanumeric을 사용해도 됨.
     */
    fun getRandomSring(len: Int, randomStr: String = "abcdefghijklmnopqrstuvwxyz123456789"): String {
        return (0 until len).map { Random.nextInt(randomStr.length) }.map { randomStr[it] }.joinToString()
    }

    /** 랜덤 비번 생성 */
    fun generateRandomPassword(length: Int = 12): String {
        require(length >= 4) { "비밀번호 길이는 최소 4자 이상이어야 합니다." }

        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#\$%^&*()-_=+[]{}<>?"
        val allChars = upper + lower + digits + symbols
        val random = SecureRandom()

        // 각 종류 최소 1개씩 보장
        val passwordChars = mutableListOf(
            upper[random.nextInt(upper.length)],
            lower[random.nextInt(lower.length)],
            digits[random.nextInt(digits.length)],
            symbols[random.nextInt(symbols.length)]
        )

        // 나머지 자리 랜덤 채우기
        repeat(length - 4) {
            passwordChars += allChars[random.nextInt(allChars.length)]
        }

        // 랜덤 순서 섞기
        return passwordChars.shuffled(random).joinToString("")
    }

}
