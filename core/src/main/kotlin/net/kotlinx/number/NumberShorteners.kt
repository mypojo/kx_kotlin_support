package net.kotlinx.number

import kotlin.math.pow

/**
 * 62진수 문자열 기반의 간단 도구 (고정 자릿수) -> 특별한 이유가 없다면 범용적으로 이거 쓰면됨
 * 62진수로 표현한 7자리 최대값은 ZZZZZZZ -> 10진수로 3521614606207
 * 총 7자리의 문자를 사용하여, 총 3조 5천개 정도의 URL 매핑 가능
 * @see net.kotlinx.number.NumberShortener
 *  */
object NumberShorteners {

    /** 자주 사용되는 기본 문자욜 */
    private val BASE62_CHARACTERS: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val BASE = BASE62_CHARACTERS.size.toLong()

    /** 랜덤한 문자열 리턴 */
    fun randomBase62(size: Int) = (1..size).map { BASE62_CHARACTERS[kotlin.random.Random.nextInt(0, BASE62_CHARACTERS.size)] }.joinToString("")

    /** 62진수 문자열 리턴 */
    fun toBase62(num: Long): String {
        check(num >= 0) { "0 이상의 정수만 가능합니다." }
        val lines = mutableListOf<Long>()
        var currentNum = num

        while (currentNum >= BASE) {
            lines += Math.floorMod(currentNum, BASE)
            currentNum = Math.floorDiv(currentNum, BASE)
        }
        lines += currentNum
        return lines.map { BASE62_CHARACTERS[it.toInt()] }.joinToString("")
    }

    fun fromBase62(num: String): Long = num.toCharArray().mapIndexed { index, c -> BASE62_CHARACTERS.indexOf(c) * BASE.toDouble().pow(index).toLong() }.sum()

}
