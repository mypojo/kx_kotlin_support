package net.kotlinx.core.collection

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.kotlinx.core.test.KotestUtil
import net.kotlinx.core.test.init

class MapSupportKtTest : BehaviorSpec({

    init(KotestUtil.FAST)

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

})