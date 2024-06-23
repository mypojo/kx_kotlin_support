package net.kotlinx.id

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.number.size

class IdMockGeneratorTest : BeSpecLog() {


    init {
        initTest(KotestUtil.FAST)

        Given("IdMockGenerator") {
            val range = 10 until 100L
            println(range.toList())

            Then("첫 값은 ${range.first}부터 시작함") {
                val generator = IdMockGenerator("테스트 사용자", range)
                generator.nextval() shouldBe range.first
            }

            Then("${range.size}개를 채번함") {
                val generator = IdMockGenerator("테스트 사용자", range)
                val ids = range.map { generator.nextval() }
                ids.size shouldBe range.size
                ids.sum() shouldBe 4905
            }

            Then("${range.size + 1}개를 채번함 -> 초과 에러 발생") {
                val generator = IdMockGenerator("테스트 사용자", range)
                shouldThrow<IllegalStateException> {
                    val range2 = range.first until range.last + 2
                    range2.last shouldBe range.last + 1
                    range2.map { generator.nextval() }
                }
            }
        }
    }

}
