package net.kotlinx.time

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class LocalDatetimeSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("일단 일반적인 사용") {
            Then("특정 단위로 truncated") {
                val now = LocalDateTime.now()
                val truncated = now.truncatedTo(ChronoUnit.MINUTES)
                truncated.toKr01() shouldBe contain("00초")
                log.info { "${now.toKr01()} -> ${truncated.toKr01()}" }
            }
        }
    }


}