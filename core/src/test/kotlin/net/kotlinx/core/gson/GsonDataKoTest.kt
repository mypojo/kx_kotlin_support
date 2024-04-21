package net.kotlinx.core.gson

import io.kotest.core.spec.style.BehaviorSpec
import mu.KotlinLogging

internal class GsonDataKoTest : BehaviorSpec({

    val log = KotlinLogging.logger {}

    val asd by lazy {
        println("===늦은로딩==")
        "xxx"
    }

    given("a broomstick") {
        `when`("I sit on it") {
            then("I should be able to fly") {
                // test code
                log.info { "================= 1 $asd" }
                //3 shouldBe 4
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