package net.kotlinx.core.number

import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test
import java.util.*

class NumberShortenerTest : TestRoot() {

    @Test
    fun test() {

        log.info("2자리 조합 가능 번호  {}", NumberShortener.sizeOfNum(2))
        log.info("3자리 조합 가능 번호  {}", NumberShortener.sizeOfNum(3))
        log.info("6자리 조합 가능 번호  {}", NumberShortener.sizeOfNum(6))
        test(1)
        test(2)
        test(3)
        test(565878)
        test(9999999999L)
        test(56800235583L)
        //test(99999999999L);
        //test(99999999999L);
        val r = Random()
        test(3733608)
        test(Math.abs(r.nextInt(100)).toLong())
        test(Math.abs(r.nextInt()).toLong())
        test(3733608)


        log.info("pFkraa =>  {}", NumberShortener.toLong("pFkraa"))

    }

    fun test(num: Long) {
        val str = NumberShortener(6).toPadString(num)
        if (str == null) {
            log.warn("변환실패. 너무 큰 수 {}", num)
            return
        }
        val result = NumberShortener.toLong(str)
        log.info("숫지 {} => {} 로 변환됨. 역변환 결과 {}", num, str, result)
        check(result == num)
    }

}