package net.kotlinx.core.gson

import net.kotlinx.core.number.halfUp
import net.kotlinx.test.TestLevel01
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class GsonSetTest : TestRoot() {

    data class Poou(
        var name: String? = null,
        var age: Int? = null,
        var cnt: Long? = null,
        var time: LocalDateTime? = null,
        var parent: Poou? = null,
    )

    val poo1 = Poou().apply {
        name = "영감님"
        age = 75
        cnt = 99987
        time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS) //밀리초 생략
        parent = Poou().apply {
            name = "할매"
            age = 132
        }
    }

    @Test
    fun `asd`() {
        val halfUp = BigDecimal(345453435.toDouble() / 123123).halfUp(1)
        println(halfUp)
        println(halfUp.toPlainString())
    }

    @TestLevel01
    fun `객체변환`() {
        val gsonData = GsonData.fromObj(poo1)
        println(gsonData)
        val poo2 = gsonData.fromJson<Poou>()
        check(poo1 == poo2) { "두개가 같아요" }
    }

    @TestLevel01
    fun `map변환`() {
        val map = GsonData.fromObj(poo1).fromJson<Map<String, Any>>()
        //val map = GsonData.fromObj(poo1).fromJson<Map<String, Any>>()
        println(map)
        val poo2 = GsonData.fromObj(map).fromJson<Poou>()
        check(poo1 == poo2) { "두개가 같아요" }


    }

}