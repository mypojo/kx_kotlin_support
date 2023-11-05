package net.kotlinx.google.otp

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class GoogleOtpTest : TestRoot() {

    @Test
    fun `키 생성`() {

        val generateSecretKey = GoogleOtp.generateSecretKey()
        println(generateSecretKey)

    }

    @Test
    fun `토큰 검증`() {

        println(GoogleOtp.checkCode("UhiIgKAStJDUMwWf0fmVQymT0nk=", 377735))

    }


}