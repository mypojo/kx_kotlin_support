package net.kotlinx.core.string

import kotlin.random.Random

object RandomStringUtil {

    /**
     * n개의 랜덤 문자열을 가져온다. 커먼즈 넣기 싫어서 만들었음
     * RandomStringUtils.randomAlphanumeric을 사용해도 됨.
     */
    fun getRandomSring(len: Int, randomStr: String = "abcdefghijklmnopqrstuvwxyz123456789"): String {
        return (0 until len).map { Random.nextInt(randomStr.length) }.map { randomStr[it] }.joinToString()
    }

}
