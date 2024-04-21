package net.kotlinx.core.string

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.Matcher
import io.kotest.matchers.compose.all
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.containADigit
import io.kotest.matchers.string.startWith
import mu.KotlinLogging
import net.kotlinx.core.concurrent.sleep
import kotlin.time.Duration.Companion.seconds

@Tags("L1")
//@Ignored  //전체 비활성화 가능
internal class KotestDemo : BehaviorSpec({

    val log = KotlinLogging.logger {}

    val dir = tempdir()

    val asd by lazy {
        println("===늦은로딩 2==")
        "xxx"
    }

    val validPassword = Matcher.all(
        containADigit(), contain(Regex("[a-z]")), contain(Regex("[A-Z]"))
    )

    given("기본적인 코테스트 사용법") {
        `when`("예외 사유 적주기") {
            then("간단 메세지") {
                var xx: String? = "Xx"
                withClue("널이면 안댐") {
                    xx shouldNotBe null
                }
            }
            then("DB 등의 , 리소스가 들어가는 메세지") {

                true shouldBe true

                var xx: String? = "xx"
                //var xx: String? = null
                withClue({
                    //실패할때만 이게 실행되어야 하는데.. 안되는듯. 내가 확장해야지..
                    println("DB 로드됨...")
                    3.seconds.sleep()
                    println("DB 로드됨!!!!")
                    var id = "..."
                    "Name should be present (user_id=${id})"
                }) {
                    xx shouldNotBe null
                }
//                msg.asClue {
//                    xx shouldNotBe null
//                }
//                exceptionInfoFromDB.asClue {
//
//                }
            }
        }
    }

    given("a broomstick") {
        `when`("I sit on it") {
            then("I should be able to fly") {


                //"asd" shouldBe validPassword

                // test code
                log.info { "================= KotestDemo $asd" }
                3.seconds.sleep()
                log.info { "================= KotestDemo 2 $asd" }

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