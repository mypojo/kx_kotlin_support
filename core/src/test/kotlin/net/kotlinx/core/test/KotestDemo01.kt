package net.kotlinx.core.test

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.Matcher
import io.kotest.matchers.compose.all
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.containADigit
import io.kotest.matchers.string.startWith


internal class KotestDemo01 : BehaviorSpec({

    addTag(KotestUtil.FAST)

    given("기본기능 확인") {
        `when`("리소스관련") {
            then("예외처리") {
                val exception = shouldThrow<IllegalAccessException> {
                    throw IllegalAccessException("Something went wrong")
                }
                exception.message should startWith("Something went wrong")
            }
        }

        `when`("리소스관련") {
            then("임시폴더는 자동상제됨") {
                val dir = tempdir()
                dir.isDirectory shouldBe true
            }
            then("I should be able to fly 22") {

            }
        }
        xwhen("이런식으로 비활성화 가능") {

        }
        `when`("then 비활성화 테스트") {
            xthen("이 코드는 실행되지 않음") {}
        }
    }

    given("매처") {
        `when`("매쳐 조합해서 재사용 가능") {
            then("비밀번호 검증") {
                val passwordMatcher = Matcher.all(
                    containADigit(), contain(Regex("[a-z]")), contain(Regex("[A-Z]"))
                )
                "abcD123" should passwordMatcher
                "abcd123" shouldNot passwordMatcher
            }
        }
    }

})

