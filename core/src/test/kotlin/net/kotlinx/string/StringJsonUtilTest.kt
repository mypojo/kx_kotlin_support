package net.kotlinx.string

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class StringJsonUtilTest : BeSpecLight() {

    init {

        initTest(KotestUtil.FAST)

        given("StringJsonUtil.cleanJsonText") {

            `when`("앞뒤 백틱 제거 테스트") {
                then("```json으로 감싼 경우") {
                    val input = "```json\n{\"name\": \"test\"}\n```"
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe "{\"name\": \"test\"}"
                }

                then("```만으로 감싼 경우") {
                    val input = "```\n{\"name\": \"test\"}\n```"
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe "{\"name\": \"test\"}"
                }

                then("백틱이 없는 경우") {
                    val input = "{\"name\": \"test\"}"
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe "{\"name\": \"test\"}"
                }
            }

            `when`("공백 및 개행 제거 테스트") {
                then("앞뒤 공백과 개행 제거") {
                    val input = "  \n  {\"name\": \"test\"}  \n  "
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe "{\"name\": \"test\"}"
                }

                then("탭 문자 제거") {
                    val input = "\t{\"name\": \"test\"}\t"
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe "{\"name\": \"test\"}"
                }
            }

            `when`("엣지 케이스 테스트") {
                then("빈 문자열") {
                    val input = ""
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe ""
                }

                then("공백만 있는 경우") {
                    val input = "   "
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe ""
                }

                then("백틱만 있는 경우") {
                    val input = "```"
                    val result = StringJsonUtil.cleanJsonText(input)
                    result shouldBe ""
                }

            }
        }
    }
}