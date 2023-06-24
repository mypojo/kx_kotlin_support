package net.kotlinx.module.reflect

import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ReflectionUtilTest {

    enum class PooType { A, B; }

    data class Poo(
        val name2: String,
        val group: String,
        val age: Int,
        val Ko: Int,
        val pooType: PooType,

        ) {
        fun toPooDto() = ReflectionLineUtil.convertTo(this, PooDto::class).apply { groupname = group } //추가 사항 코딩 가능
    }

    data class PooDto(
        var groupname: String = "", //순서 변경됨
        var name2: String = "",
        var age: Int = 0,
        val po: Int = 0,
        val regTime: LocalDateTime? = null,
    ) {
        constructor() : this("") // no-arg & 어노테이션으로 대체 가능
    }

    @Test
    fun 기본테스트() {
        val poo = Poo("영감", "노인", 16, 34, PooType.A)
        val pooDto = poo.toPooDto()

        check(poo.name2 == pooDto.name2)
        check(poo.group == pooDto.groupname)
        check(poo.age == pooDto.age)

        val p1 = ReflectionLineUtil.lineToData(arrayOf("영감님", "판매부서", "12", "", "B"), Poo::class)
        val p21 = ReflectionLineUtil.lineToData(arrayOf("영감님1", "판매부서", "-2", "1231", "202212211356"), PooDto::class)
        val p22 = ReflectionLineUtil.lineToData(arrayOf("영감님2", "개발부서", "545", "874", "202512211356"), PooDto::class)
//        listOf(p1).toTextGrid().print()
//        listOf(p21, p22).toTextGrid().print()

    }

}