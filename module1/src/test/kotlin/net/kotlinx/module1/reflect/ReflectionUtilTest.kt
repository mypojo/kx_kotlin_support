package net.kotlinx.module1.reflect

import org.junit.jupiter.api.Test

internal class ReflectionUtilTest {

    data class Poo(
        val name2: String,
        val group: String,
        val age: Int,
        val Ko: Int,
    ) {
        fun toPooDto() = ReflectionUtil.convert(this, PooDto::class).apply { groupname = group } //추가 사항 코딩 가능
    }

    data class PooDto(
        var name2: String = "",
        var groupname: String = "",
        var age: Int = 0,
        val po: Int = 0,
    ) {
        constructor() : this("") // no-arg & 어노테이션으로 대체 가능
    }

    @Test
    fun 기본테스트() {
        val poo = Poo("영감", "노인", 16,34)
        val pooDto = poo.toPooDto()

        check(poo.name2 == pooDto.name2)
        check(poo.group == pooDto.groupname)
        check(poo.age == pooDto.age)

    }

}