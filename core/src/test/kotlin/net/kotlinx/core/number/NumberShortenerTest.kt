package net.kotlinx.core.number

import io.kotest.matchers.shouldBe
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.core.string.toTextGridPrint
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import kotlin.math.absoluteValue
import kotlin.random.Random

class NumberShortenerTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("NumberShortener") {
            Then("조합 가능 번호 확인") {
                log.info("2자리 조합 가능 번호  {}", NumberShortener.sizeOfNum(2))
                log.info("3자리 조합 가능 번호  {}", NumberShortener.sizeOfNum(3))
                log.info("6자리 조합 가능 번호  {}", NumberShortener.sizeOfNum(6))

            }

            val shortener = NumberShortener(6)
            Then("조합 가능 번호 확인") {

                val nums = listOf(
                    1, 2, 6, 565878, 9999999999L, 56800235581L, 56800235583L,
                    Random.nextInt().absoluteValue, Random.nextLong().absoluteValue,
                )
                listOf("숫자", "변환결과").toTextGridPrint {
                    nums.map {
                        try {
                            val result = shortener.toPadString(it.toLong())
                            val fromResult = NumberShortener.toLong(result)
                            fromResult shouldBe it
                            arrayOf(it, result)
                        } catch (e: Exception) {
                            arrayOf(it, e.toSimpleString())
                        }
                    }
                }
            }
        }
    }
}