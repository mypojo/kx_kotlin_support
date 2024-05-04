package net.kotlinx.core.collection

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class CollectionSupportKtTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("mapNotEmpty") {
            Then("널이거나 빈값 제외") {
                val demo = listOf("a", null, "", "b")
                check(demo.mapNotEmpty { it }.size == 2)
            }
        }
    }
}