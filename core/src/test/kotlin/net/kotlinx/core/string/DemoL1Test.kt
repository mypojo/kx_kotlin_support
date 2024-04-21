package net.kotlinx.core.string

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.Tag
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.startWith
import mu.KotlinLogging

@Tags("L1")
//@Ignored  //전체 비활성화 가능
internal class DemoL1Test : BehaviorSpec({

    val log = KotlinLogging.logger {}

    tags(Tag("L2"))


    val asd by lazy {
        println("===늦은로딩 2==")
        "xxx"
    }

    given("a broomstick") {
        `when`("I sit on it") {
            then("I should be able to fly") {
                // test code
                log.info { "================= 1 $asd" }
                //3 shouldBe 4

                val exception = shouldThrow<IllegalAccessException> {
                    throw IllegalAccessException("Something went wrong")
                }
                exception.message should startWith("Something went wrong")
            }
            then("I should be able to fly 22") {
                // test code
                log.info { "================= 2 $asd" }
                //3 shouldBe 4
            }
        }
        `xwhen`("이런식으로 비활성화 가능") {
            then("it should come back") {
                // test code
            }
        }
    }

})