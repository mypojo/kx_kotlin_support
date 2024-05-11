package net.kotlinx.number

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.time.toIso
import kotlin.time.Duration.Companion.seconds

class LongSupportKtTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("LongSupport") {
            Then("toLocalDateTime") {
                1681869805.seconds.inWholeMilliseconds.toLocalDateTime().toIso() shouldBe "2023-04-19T11:03:25"
            }
        }
    }
}