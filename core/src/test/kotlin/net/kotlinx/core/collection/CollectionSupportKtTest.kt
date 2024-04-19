package net.kotlinx.core.collection

import net.kotlinx.test.TestLevel01
import net.kotlinx.test.TestRoot

class CollectionSupportKtTest : TestRoot() {

    @TestLevel01
    fun `널이거나 빈값 제외`() {
        val demo = listOf("a", null, "", "b")
        check(demo.mapNotEmpty { it }.size == 2)
    }
}