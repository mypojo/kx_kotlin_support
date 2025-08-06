package net.kotlinx.spring.servlet

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class HttpAuthUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("HttpAuthUtil") {

            Then("인증테스트-일반 ID/비번") {
                val auth = "Basic bmhuOjEyMzQ="
                val pair = HttpAuthUtil.validate(auth)
                pair shouldNotBe null
                pair!!.first shouldBe "nhn"
                pair!!.second shouldBe "1234"
            }
        }
    }


}