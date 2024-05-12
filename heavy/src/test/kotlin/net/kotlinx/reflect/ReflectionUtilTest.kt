package net.kotlinx.reflect

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.time.LocalDateTime

internal class ReflectionUtilTest : BeSpecLog() {

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

    init {
        initTest(KotestUtil.FAST)

        Given("ReflectionLineUtil") {
            Then("기본테스트") {
                val poo = Poo("영감", "노인", 16, 34, PooType.A)
                val pooDto = poo.toPooDto()

                poo.name2 shouldBe pooDto.name2
                poo.group shouldBe pooDto.groupname
                poo.age shouldBe pooDto.age

                val p1 = ReflectionLineUtil.lineToData(arrayOf("영감님", "판매부서", "12", "", "B"), Poo::class)
                p1.name2 shouldBe "영감님"
                val p21 = ReflectionLineUtil.lineToData(arrayOf("영감님1", "판매부서", "-2", "1231", "202212211356"), PooDto::class)
                p21.name2 shouldBe "판매부서"

            }
        }
    }


}