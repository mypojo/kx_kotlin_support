package net.kotlinx.kotest

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.*
import io.kotest.matchers.compose.all
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.containADigit
import io.kotest.matchers.string.startWith

internal class KotestDemo : BeSpecLog() {

    init {

        initTest(KotestUtil.FAST)

        Given("기본적인 코테스트 사용법") {

            When("기본제공기능") {
                xthen("이런식으로 비활성화 가능") {
                    log.warn { "실행되지 않음" }
                }
            }

            When("리소스관련") {
                Then("임시폴더는 자동상제됨") {
                    val dir = tempdir()
                    dir.isDirectory shouldBe true
                }
            }

            When("예외 관련") {
                Then("간단 메세지") {
                    var xx: String? = "Xx"
                    withClue("널이면 안댐") { xx shouldNotBe null }
                }
                Then("DB 등의 , 리소스가 들어가는 메세지 -> 늦은로딩") {

                    log.warn { "내가 잘못이해한것인지? 실무에 안쓸거같으니 그냥 넘어감" }
                    withClue({
                        log.warn { " -> 예외 상세 정보를 조회하기 위해서 DB 조회..." }
                        "Name should be present (user_id=kim)"
                    }) {
                        //null shouldNotBe null
                        "x" shouldNotBe null
                    }
                }
                Then("예외 예상") {
                    val exception = shouldThrow<IllegalAccessException> {
                        throw IllegalAccessException("Something went wrong")
                    }
                    exception.message should startWith("Something went wrong")
                }
            }
        }

        Given("매처") {

            val validPassword = Matcher.all(
                containADigit(), contain(Regex("[a-z]")), contain(Regex("[A-Z]"))
            )

            When("매쳐 조합해서 재사용 가능") {
                Then("비밀번호 검증") {
                    val passwordMatcher = Matcher.all(
                        containADigit(), contain(Regex("[a-z]")), contain(Regex("[A-Z]"))
                    )
                    "abcD123" should passwordMatcher
                    "abcd123" shouldNot passwordMatcher
                }
            }
        }
    }

}