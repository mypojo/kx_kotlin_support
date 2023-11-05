package net.kotlinx.google.otp

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class GoogleOtpTest : TestRoot() {

    @Test
    fun `키 생성`() {


    }

    @Test
    fun `토큰 검증`() {

        println(GoogleOtp.checkCode("OMUGBWFMJMHKWQ7IIOOKX6GEHWPGRL5M", 116260))

    }


}