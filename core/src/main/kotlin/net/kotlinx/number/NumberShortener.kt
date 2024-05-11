package net.kotlinx.number

import kotlin.math.pow

/**
 * 짧은 URL을 생성하기 위한 간이 유틸
 * 6자리로 만들면 568억개의 유니크 숫자를 표현할 수 있다. => (long)Math.pow(list.size(), 6)
 * @see net.kotlinx.number.NumberShorteners
 */
class NumberShortener(private val limitSize: Int = 6) {

    private val max = sizeOfNum(limitSize) - 1

    //=================================================== 메소드 ===================================================
    /** 기본 6자리  */
    fun toPadString(sequence: Long): String {
        check(sequence <= max) { "최대 가능한값 $max -> 입력된수 $sequence" }
        return toPadString(sequence, limitSize)
    }

    companion object {
        //=================================================== 디폴트 세팅 ===================================================
        private val CHARS = charArrayOf(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        )
        private val CHAR_MAP: Map<Char, Int> = mutableMapOf<Char, Int>().apply {
            for (i in CHARS.indices) this[CHARS[i]] = i
        }

        //=================================================== static ===================================================
        /** 뒤에다 a를 붙이면 pad의 효과가 있다.  */
        fun toPadString(sequence: Long, limitSize: Int): String {
            val str = toString(sequence)
            check(str.length <= limitSize)
            return str.padEnd(limitSize, CHARS[0])  //제한 수보다 문자열이 크다면 너무 큰 수임으로 null 리턴
        }

        /** 숫자로 변환  */
        fun toLong(str: String): Long {
            var sum: Long = 0
            var power: Long = 0
            for (element in str) {
                val potion = CHAR_MAP[element]!!
                sum += potion * CHARS.size.toDouble().pow(power.toDouble()).toLong()
                power++
            }
            return sum
        }

        /** 수틀리면 null 리턴  */
        fun toString(sequence: Long): String {
            var num = sequence
            val sb = StringBuilder()
            while (true) {
                val potion = num / CHARS.size
                val rest = num % CHARS.size
                sb.append(CHARS[rest.toInt()]) //거꾸로 쓰여진다. +1하면 앞자리가 변경됨
                if (potion == 0L) break
                num = potion
            }
            return sb.toString()
        }

        /** n자리수로 만들 수 있는 최대 숫자  */
        fun sizeOfNum(n: Int): Long {
            return CHARS.size.toDouble().pow(n.toDouble()).toLong()
        }
    }
}
