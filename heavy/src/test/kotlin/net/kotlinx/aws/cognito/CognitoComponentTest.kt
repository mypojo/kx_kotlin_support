package net.kotlinx.aws.cognito

import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import kotlinx.coroutines.flow.toList
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.RandomStringUtil
import net.kotlinx.string.StringHpUtil
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.util.*
import kotlin.random.Random


class CognitoComponentTest : BeSpecLight() {

    companion object {
        private const val POOL_ID = "ap-northeast-2_PeLL1FobS"
    }

    val comp by lazy {
        CognitoComponent(POOL_ID).apply { this.aws = aws49 }
    }

    val compSession by lazy {
        CognitoSessionComponent(POOL_ID, "7lqsshvhpstcbsgdgt5hg0as9i").apply { this.aws = aws49 }
    }

    val tokenSigningKey = "https://cognito-idp.ap-northeast-2.amazonaws.com/ap-northeast-2_PeLL1FobS/.well-known/jwks.json"
    val decoder: NimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(tokenSigningKey).build()!!

    init {
        initTest(KotestUtil.IGNORE)

        Given("CognitoComponent") {
            printName()
            val username = UUID.randomUUID().toString()
            log.info { "username : ${username}" }
            val pwd = RandomStringUtil.generateRandomPassword(16)
            val pwd2 = RandomStringUtil.generateRandomPassword(16)
            val hp = StringHpUtil.toE164Kr("010-${RandomStringUtil.getRandomNumber(4)}-${RandomStringUtil.getRandomNumber(4)}")
            val email = "seunghan.shin${Random.nextInt(100)}@ad.com"

            When("사용자") {
                printName()
                Then("사용자 생성") {
                    comp.adminCreateUserDefault(username, email, pwd, null, "api")
                }
                Then("사용자 수정") {
                    comp.adminUpdateUserDefault(username, null, hp)
                }
                Then("사용자 리스팅") {
                    val allUsers = comp.listAllUsers().toList().flatMap { it.users!! }
                    log.info { allUsers.joinToString("\n") }
                }
                Then("사용자 상세정보") {
                    val user = comp.adminGetUser(username)
                    log.info { "user detail: $user" }
                }
                Then("사용자 비밀번호 수정") {
                    comp.adminSetUserPasswordDefault(username, pwd2)
                }
            }

            When("로그인 3종") {
                printName()
                Then("로그인1") {
                    val response = compSession.initiateAuth(username, pwd2)
                    log.info { "accessToken: ${response.authenticationResult!!.accessToken}" }
                    log.info { "refreshToken: ${response.authenticationResult!!.refreshToken}" }
                }
                Then("로그인2 & 토큰검증") {
                    val response = compSession.initiateAuth(hp, pwd2)
                    log.info { "login response: $response" }

                    val decoded = decoder.decode(response.authenticationResult!!.accessToken)!!
                    // 파싱된 토큰 정보 검증
                    val claims = decoded.claims

                    // 기본 클레임 유효성
                    decoded.subject shouldNotBe null
                    decoded.subject!!.shouldNotBeBlank()

                    // 토큰 용도(access token) 확인
                    (claims["token_use"] as? String) shouldContain "access"

                    // 필수 클레임 존재 여부 확인
                    claims["client_id"] shouldNotBe null
                    claims["username"] shouldNotBe null
                    (claims["iss"] as? String)?.shouldContain("https://cognito-idp.")

                    // 만료/발급 시간 일관성 확인
                    val exp = requireNotNull(decoded.expiresAt) { "JWT exp(만료시간) 클레임이 없습니다" }.epochSecond
                    val iat = requireNotNull(decoded.issuedAt) { "JWT iat(발급시간) 클레임이 없습니다" }.epochSecond
                    exp shouldBeGreaterThan iat

                }
                Then("로그인3 & 리프레시") {
                    val response = compSession.initiateAuth(email, pwd2)
                    log.info { "login response for refresh: $response" }
                    val newResp = compSession.refreshTokens(response.authenticationResult!!.refreshToken!!)
                    log.info { "refreshed response: $newResp" }
                }
            }

            When("정리") {
                printName()
                comp.adminDeleteUser(username)
            }
        }
    }
}
