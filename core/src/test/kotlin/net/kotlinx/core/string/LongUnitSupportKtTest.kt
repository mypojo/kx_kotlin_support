package net.kotlinx.core.string

import net.kotlinx.core.number.toSiText
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

internal class LongUnitSupportKtTest : TestRoot() {


    @Test
    fun `기본테스트`() {

        println(19915640000.toSiText())

    }

}