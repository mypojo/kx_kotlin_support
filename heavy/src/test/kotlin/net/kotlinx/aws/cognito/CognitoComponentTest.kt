package net.kotlinx.aws.cognito

import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import kotlinx.coroutines.flow.toList
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.spring.security.CognitoJwtComponent
import net.kotlinx.string.RandomStringUtil
import net.kotlinx.string.StringHpUtil
import java.util.*
import kotlin.random.Random


class CognitoComponentTest : BeSpecLight() {

    companion object {
        private const val POOL_ID = "ap-northeast-2_BVal8frOo"
    }

    val comp by lazy {
        CognitoComponent(POOL_ID).apply { this.aws = aws49 }
    }

    val compSession by lazy {
        CognitoSessionComponent(POOL_ID, "3eajd74t010m4j3a7e31p29uuk").apply { this.aws = aws49 }
    }

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

            val jwt = CognitoJwtComponent(POOL_ID)

            When("사용자 관리 (수드비)") {
                printName()
                Then("사용자 생성 (그룹 포함)") {
                    val groupName = "test-group"
                    val resp = comp.adminCreateUserDefault(username, email, pwd, null, groupName)

                    val user = comp.findUserByUsername(username)
                    user.userAttributes!!.any { it.name == "email" && it.value == email } shouldBe true
                    resp.sub shouldBe user.sub

                    val userBySub = comp.findUserBySub(resp.sub!!)
                    userBySub?.username shouldBe username

                    log.info { "MFA 설정 여부: ${userBySub?.mfaOptions}" }
                }
                Then("사용자 MFA 활성화 여부 체크 (findUserBySub)") {
                    val user = comp.findUserByUsername(username)
                    val userBySub = comp.findUserBySub(user.sub!!)

                    // 기본적으로는 MFA가 비활성화되어 있음
                    log.info { "사용자 sub: ${user.sub}, MFA Options: ${userBySub?.mfaOptions}" }
                    // mfaOptions가 null이거나 비어있으면 MFA가 설정되지 않은 것
                    userBySub!!.mfaOptions.isNullOrEmpty() shouldBe true //실제로는 null
                }
                Then("사용자 중복 생성 (기존 사용자 조회 테스트)") {
                    comp.adminCreateUserDefault(username, email, pwd, null, null)
                    val user = comp.findUserByUsername(username)
                    user.username shouldBe username
                }
                Then("사용자 속성 수정") {
                    comp.adminUpdateUserDefault(username, email, hp)
                    val user = comp.findUserByUsername(username)
                    user.userAttributes!!.any { it.name == "phone_number" && it.value == hp } shouldBe true
                }
                Then("사용자 비밀번호 강제 수정") {
                    comp.adminSetUserPasswordDefault(username, pwd2)
                }
                Then("사용자 리스팅") {
                    val allUsers = comp.listAllUsers().toList().flatMap { it.users!! }
                    log.info { "전체 사용자 수: ${allUsers.size}" }
                }
                Then("사용자 리스팅 - prefix") {
                    val prefix = username.substring(0, 5)
                    val filteredUsers = comp.listUsersByUsernamePrefix(prefix).toList().flatMap { it.users!! }
                    log.info { "prefix [$prefix] 사용자 수: ${filteredUsers.size}" }
                    filteredUsers.any { it.username == username } shouldBe true
                }
                Then("사용자 상세정보 조회") {
                    val user = comp.findUserByUsername(username)
                    log.info { "상세 정보: $user" }
                    user.username shouldBe username
                }
                // 로그인 테스트를 위해 삭제는 뒤로 미룸
            }

            When("로그인 및 토큰 검증") {
                printName()
                Then("로그인1") {
                    val response = compSession.initiateAuth(username, pwd2)
                    log.info { "accessToken: ${response.authenticationResult!!.accessToken}" }
                    log.info { "refreshToken: ${response.authenticationResult!!.refreshToken}" }

                    val idUser = jwt.parseIdToken(response.authenticationResult!!.idToken!!)
                    idUser.email shouldBe email

                    val accessTokenInfo = jwt.parseAccessToken(response.authenticationResult!!.accessToken!!)
                    accessTokenInfo.username shouldBe idUser.username
                    accessTokenInfo.tokenUse shouldBe "access"
                }
                Then("로그인2 & 토큰검증") {
                    //val response = compSession.initiateAuth(hp, pwd2)  //휴대전화 안쓰게 수정
                    val response = compSession.initiateAuth(username, pwd2)
                    log.info { "login response: $response" }

                    val accessTokenInfo = jwt.parseAccessToken(response.authenticationResult!!.accessToken!!)

                    // 기본 정보 유효성
                    accessTokenInfo.sub.shouldNotBeBlank()
                    accessTokenInfo.username.shouldNotBeBlank()

                    // 토큰 용도(access token) 확인
                    accessTokenInfo.tokenUse shouldBe "access"

                    // 필수 필드 존재 여부 확인
                    accessTokenInfo.clientId.shouldNotBeBlank()

                    // 추가 검증이 필요한 경우 직접 decoder 사용 (예: iss, exp 등은 현재 데이터 클래스에 없음)
                    val decoded = jwt.decoder.decode(response.authenticationResult!!.accessToken)!!
                    (decoded.claims["iss"] as? String)?.shouldContain("https://cognito-idp.")

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

            When("사용자 삭제") {
                printName()
                Then("사용자 삭제 실행") {
                    comp.adminDeleteUser(username)
                }
            }
        }
    }
}
