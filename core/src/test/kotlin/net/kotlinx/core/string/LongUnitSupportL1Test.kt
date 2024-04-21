package net.kotlinx.core.string

import net.kotlinx.core.number.toSiText
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

internal class LongUnitSupportL1Test : TestRoot() {

    @Test
    fun `기본테스트`() {
        println(19915640000.toSiText())
    }

}