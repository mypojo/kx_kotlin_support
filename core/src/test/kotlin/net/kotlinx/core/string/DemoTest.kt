package net.kotlinx.core.string

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import mu.KotlinLogging

@Tags("L2")
//@Ignored  //전체 비활성화 가능
internal class DemoTest : BehaviorSpec({

    val log = KotlinLogging.logger {}

    given("a broomstick") {
        `when`("I sit on it") {
            then("I should be able to fly") {
                // test code
                log.info { "================= xx" }
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