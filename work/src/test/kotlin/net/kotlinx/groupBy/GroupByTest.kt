package net.kotlinx.groupBy


import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight


class GroupByTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("주어진 데이터를 나이로 그룹화 하고싶음") {

            data class Person(val name: String, val age: Int)

            val people = listOf(
                Person("Alice", 29),
                Person("Bob", 29),
                Person("Charlie", 31),
                Person("David", 31),
                Person("Edward", 29)
            )

            When("groupingBy") {
                val group = people.groupBy { it.age }
                Then("즉시 실제 그룹화됨 -> 메모리 많이먹음s") {
                    group.forEach { (age, list) ->
                        println("Age $age: ${list.size} people")
                    }
                }
            }

            When("groupingBy") {

                Then("lazy operation -> 메모리 적게먹음") {
                    val groupedByAge = people.groupingBy { it.age }.eachCount()
                    groupedByAge.forEach { (age, count) ->
                        println("Age $age: $count people")
                    }
                }
            }


        }
    }

}