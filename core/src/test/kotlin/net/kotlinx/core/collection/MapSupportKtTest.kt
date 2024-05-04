package net.kotlinx.core.collection

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class MapSupportKtTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("flatten") {
            Then("플랫화됨") {
                val maps = listOf(
                    mapOf(
                        "a" to 1,
                        "b" to 2,
                    ),
                    mapOf(
                        "a" to 3,
                        "c" to 8,
                    ),
                ).flatten()
                maps.size shouldBe 3
            }
        }
    }
}