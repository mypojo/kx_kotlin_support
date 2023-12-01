package net.kotlinx.core.collection

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class MapSupportKtTest : TestRoot() {

    @Test
    fun test() {

        val maps = listOf(
            mapOf(
                "a" to 1,
                "b" to 2,
            ),
            mapOf(
                "c" to 3,
            ),
        ).flatten()

        println(maps)

    }

}