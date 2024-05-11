package net.kotlinx.google.otp

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class GoogleOtpTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("GoogleOtp") {
            Then("키 생성") {
                val generateSecretKey = GoogleOtp.generateSecretKey()
                generateSecretKey.length shouldBeGreaterThan 0
            }
            xThen("토큰 검증 -> 스마트폰에 등록후 테스트 해보세요") {
                GoogleOtp.checkCode("UhiIgKAStJDUMwWf0fmVQymT0nk=", 377735) shouldBe true
            }
        }
    }

}