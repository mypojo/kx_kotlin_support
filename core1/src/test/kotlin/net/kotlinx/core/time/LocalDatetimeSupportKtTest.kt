package net.kotlinx.core.time

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class LocalDatetimeSupportKtTest {

//    infix fun LocalDateTime.between(x: LocalDateTime): Long {
//
//        val asd = this between x
//
//
//        this.toLong() - x.toLong()
//    }


    @Test
    fun `toLocalDate2`() {
        val closedRange = "20230101".."20230131"
    }


    @Test
    fun `bt`() {
        val message = "a".."g"
        println(message)
        println("b" in message)


    }

    @Test
    fun `기본테스트`() {

        val now = LocalDateTime.now()
        println(now.toKr01())
        println(now.truncatedTo(ChronoUnit.MINUTES).toKr01())

    }
}