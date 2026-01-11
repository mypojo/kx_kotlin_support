package net.kotlinx.aws.cognito

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.RandomStringUtil
import java.util.*
import kotlin.random.Random


class CognitoComponent_MFA테스트 : BeSpecLight() {

    companion object {
        private const val POOL_ID = "ap-northeast-2_BVal8frOo"
        private const val CLIENT_ID = "3eajd74t010m4j3a7e31p29uuk"
    }

    val comp by lazy {
        CognitoComponent(POOL_ID).apply { this.aws = aws49 }
    }

    val compSession by lazy {
        CognitoSessionComponent(POOL_ID, CLIENT_ID).apply { this.aws = aws49 }
    }

    val compMfa by lazy {
        CognitoMfaComponent("통합테스트프로그램-dev").apply { this.aws = aws49 }
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("CognitoComponent MFA 테스트") {
            printName()

            val email = "seunghan.shin${Random.nextInt(100)}@ad.com"

            Then("1. 사용자 생성 및 정보 출력") {
                val username = UUID.randomUUID().toString()
                val pwd2 = RandomStringUtil.generateRandomPassword(16)
                comp.adminCreateUserDefault(username, email, pwd2, null, null)
                comp.adminSetUserPasswordDefault(username, pwd2)

                log.info { "========================================" }
                log.info { "username : $username" }
                log.info { "password : $pwd2" }
                log.info { "========================================" }

                val user = comp.findUserByUsername(username)
                user.username shouldBe username
            }

            When("2. MFA 테스트") {
                val username = "86e0a147-976a-43db-a53e-1dd57e1eb005"
                val pwd2 = "NgUm<CZ7M47b%&Yi"
                Then("MFA 등록 (소프트웨어 토큰)") {
                    //https://www.deepnetsecurity.com/tools/otp-qr-generator/?utm_source=chatgpt.com
                    //위 사이트에서 QR 변환가능
                    val authResp = compSession.initiateAuth(username, pwd2)
                    val accessToken = authResp.authenticationResult?.accessToken!!
                    val otpUri = compMfa.associateSoftwareToken(accessToken, "홍길동")
                    otpUri.shouldNotBeBlank()
                    log.info { "OTP URI: $otpUri" }
                }

                Then("MFA 검증 (수동으로 번호 입력 필요시 수정해서 사용)") {
                    val authResp = compSession.initiateAuth(username, pwd2)
                    val accessToken = authResp.authenticationResult?.accessToken!!
                    val dummyCode = "152572"
                    val verifyResp = compMfa.verifySoftwareToken(accessToken, dummyCode)
                    log.info { "verifyResp: $verifyResp" }
                }

                Then("MFA가 로그인시 필수인지 확인") {
                    val user = comp.findUserByUsername(username)!!
                    log.info { "mfaOptions: ${user.mfaOptions}" }
                    println(user.mfaOptions.isNullOrEmpty())
                }
            }
        }
    }
}
