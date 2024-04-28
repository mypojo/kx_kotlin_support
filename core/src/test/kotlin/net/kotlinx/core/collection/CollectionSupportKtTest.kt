package net.kotlinx.core.collection

import io.kotest.core.spec.style.BehaviorSpec
import net.kotlinx.core.test.KotestUtil
import net.kotlinx.core.test.init

class CollectionSupportKtTest : BehaviorSpec({

    init(KotestUtil.FAST)

    Given("mapNotEmpty") {
        Then("널이거나 빈값 제외") {
            val demo = listOf("a", null, "", "b")
            check(demo.mapNotEmpty { it }.size == 2)
        }
    }

})