package net.kotlinx.reflect

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class BeanTest : BeSpecLog() {

    data class Poo1(
        val name: String,
    ) {

        var age: Int? = null
        var group: String? = null
    }

    class PooDto1 {
        var name: String? = null
        var age: Int? = null
        var tag: String? = null
    }

    class PooDto2(
        var name: String,
        var age: Int?,
    ) {
        var tag: String? = null
    }

    data class PooDto3(
        var name: String? = null,
        var age: Int? = null,
        var tag: String? = null,
    )

    init {
        initTest(KotestUtil.FAST)

        Given("Bean") {
            Then("간단 사용 테스트") {
                val p1 = Poo1("홍길동").apply {
                    age = 15
                    group = "테스트"
                }

                Bean(p1).also {
                    it["name"] shouldBe "홍길동"
                    it["age"] shouldBe 15

                    it.put("age", 878)
                    it["age"] shouldBe 878
                }

                Bean(p1).convert(PooDto1::class).also {
                    Bean(it).toTextGrid().print()
                    check(it.name == p1.name)
                }
                Bean(p1).convert(PooDto2::class).also {
                    Bean(it).toTextGrid().print()
                    check(it.name == p1.name)
                }

                val fromLine = Bean.fromLine(PooDto3::class, listOf("김철수", "26", "myTag"))
                Bean(fromLine).toTextGrid().print()
            }
        }
    }

}