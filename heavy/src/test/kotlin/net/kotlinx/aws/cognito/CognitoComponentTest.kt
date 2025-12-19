package net.kotlinx.aws.cognito

import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import kotlinx.coroutines.flow.toList
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.StringHpUtil
import net.kotlinx.string.print
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

class CognitoComponentTest : BeSpecLight() {

    val comp by lazy {
        //demo
        CognitoComponent("ap-northeast-2_PeLL1FobS", "7lqsshvhpstcbsgdgt5hg0as9i").apply {
            this.aws = aws49
        }
    }

    val compSession by lazy {
        //demo
        CognitoSessionComponent("ap-northeast-2_PeLL1FobS", "7lqsshvhpstcbsgdgt5hg0as9i").apply {
            this.aws = aws49
        }
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("CognitoComponent") {
            val username = "006250de-c524-45b1-86c0-26a573bf8c59"
            val pwd = "]}9&&r@Xmt7a"
            val pwd2 = "]}9&&r@Xmt7a22"
            val hp = StringHpUtil.toE164Kr("010-1111-2222")
            val email = "seunghan.shin@ad.com"

            val tokenSigningKey = "https://cognito-idp.ap-northeast-2.amazonaws.com/ap-northeast-2_PeLL1FobS/.well-known/jwks.json"
            val decoder = NimbusJwtDecoder.withJwkSetUri(tokenSigningKey).build()

            When("사용자") {
                Then("사용자 생성") {
                    comp.adminCreateUserDefault(username, email, pwd, null, "api")
                }
                Then("사용자 수정") {
                    comp.adminUpdateUserDefault(username, null, hp)
                }
                Then("사용자 리스팅") {
                    val allUsers = comp.listAllUsers().toList().flatMap { it.users!! }
                    allUsers.print()
                }
                Then("사용자 상세정보") {
                    val user = comp.adminGetUser(username)
                    println(user)
                }
                Then("사용자 비밀번호 수정") {
                    comp.adminSetUserPasswordDefault(username, pwd2)
                }
            }

            When("로그인 3종") {
                Then("로그인1") {
                    val response = compSession.initiateAuth(username, pwd)
                    println(response.authenticationResult!!.accessToken)
                    println(response.authenticationResult!!.refreshToken)
                }
                Then("로그인2 & 토큰검증") {
                    val response = compSession.initiateAuth(hp, pwd2)
                    println(response)

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
                    println(response)
                    val newResp = compSession.refreshTokens(response.authenticationResult!!.refreshToken!!)
                    println(newResp)


                }
            }
        }
    }
}
