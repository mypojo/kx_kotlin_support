package net.kotlinx.collection

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.kotlinx.exception.KnownException
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class DoUntilSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("doUntil") {
            Then("초과시 에러") {
                shouldThrow<KnownException.StopException> {
                    doUntilNotEmpty { listOf("a") }
                }
            }
            Then("리밋까지 적용 후 합산") {
                val limit = 11
                val lists = doUntilNotEmpty(limit) { if (it == limit - 1) emptyList() else listOf(it) }
                lists.flatten().sumOf { it } shouldBe 45
            }
        }

    }

}
