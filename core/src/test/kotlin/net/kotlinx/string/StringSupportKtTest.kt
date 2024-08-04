package net.kotlinx.string

import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class StringSupportKtTest : BeSpecLight() {

    init {

        initTest(KotestUtil.FAST)

        Given("lett") {

            var input: String? = null

            When("입력값이 null인경우") {
                Then("널값 리턴됨") {
                    val result = input.lett { obj { "msg" to it } } ?: obj { "msg" to "fail" }
                    result.toGsonData()["msg"].str  shouldBe "fail"
                }
            }

            When("입력값이 empty 인경우") {
                input = ""
                Then("널값 리턴됨") {
                    val result = input.lett { obj { "msg" to it } } ?: obj { "msg" to "fail" }
                    result.toGsonData()["msg"].str  shouldBe "fail"
                }
            }

            When("입력값이 있는경우") {
                input = "성공했어요"
                Then("성공값 리턴됨") {
                    val result = input.lett { obj { "msg" to it } } ?: obj { "msg" to "fail" }
                    result.toGsonData()["msg"].str  shouldBe "성공했어요"
                }
            }
        }
    }
}