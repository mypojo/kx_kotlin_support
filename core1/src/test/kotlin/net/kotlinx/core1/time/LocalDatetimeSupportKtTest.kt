package net.kotlinx.core1.time

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class LocalDatetimeSupportKtTest {

    @Test
    fun `기본테스트`() {

        val now = LocalDateTime.now()
        println(now.toKr01())
        println(now.truncatedTo(ChronoUnit.MINUTES).toKr01())

    }
}