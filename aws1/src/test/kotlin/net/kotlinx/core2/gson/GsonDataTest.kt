package net.kotlinx.core2.gson

import net.kotlinx.aws1.TestRoot
import org.junit.jupiter.api.Test

internal class GsonDataTest : TestRoot() {
    @Test
    fun `기본테스트`() {

        val gsonData = GsonData.array().apply {
            put("a", "b")
        }
        assert(gsonData["a"] == GsonData.EMPTY)

    }
}