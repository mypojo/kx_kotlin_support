package net.kotlinx.reflect

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class BeanTest : TestRoot() {

    data class Poo1(
        val name: String
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


    @Test
    fun test() {


        val p1 = Poo1("홍길동").apply {
            age = 15
            group = "테스트"
        }

        Bean(p1).also {
            println(it["name"])
            println(it["age"])
            it.put("age", 878)
            println(it["age"])
            check(it["age"] == 878)
            println(it.get(Poo1::name))
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