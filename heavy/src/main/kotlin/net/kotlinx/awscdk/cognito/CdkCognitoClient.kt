package net.kotlinx.awscdk.cognito

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.awscdk.toCdk
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.services.cognito.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Cognito User Pool Client 구성 전용 서브클래스
 * - 풀(User Pool) 생성은 상위 클래스(CdkCognito)의 createPool 사용
 * - 클라이언트 생성 및 관련 설정을 이 클래스로 분리
 */
class CdkCognitoClient : CdkInterface {

    @Kdsl
    constructor(block: CdkCognitoClient.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "$clientName-${suff}"

    //==================================================== 클라이언트 설정 ======================================================

    lateinit var userPool: UserPool

    /** 클라이언트 이름 */
    lateinit var clientName: String

    /** 클라이언트 시크릿 생성 여부 (백엔드만 사용시 보통 false) */
    var generateSecret: Boolean = false

    /** AuthFlows 기본값: userPassword만 true */
    var authFlows: AuthFlow = AuthFlow.builder()
        .userPassword(false) // client 기반의 구버전 로그인
        .userSrp(false)  //Hosted UI 안쓰면 너무 복잡함.  서버에서 직접 구현은 추천하지 않음
        .adminUserPassword(true) //백엔드 전용 로그인
        .custom(false)
        .build()

    /** OAuth 설정 기본값. 기본권장세팅 */
    var oAuthFlows: OAuthFlows = OAuthFlows.builder()
        .authorizationCodeGrant(true)  //보안적으로 코드만 써야함
        .implicitCodeGrant(false)
        .clientCredentials(false)
        .build()

    /** 인증시 받는 정보 -> 이메일하고 기본 프로파일 요구 */
    var oAuthScopes: List<OAuthScope> = listOf(OAuthScope.OPENID, OAuthScope.EMAIL, OAuthScope.PROFILE)

    /**
     * 로그인 이후 code 받을 콜백 화이트리스트
     *  */
    lateinit var oAuthCallbackUrls: List<String>

    /**
     * auth 도메인이 로그아웃시 리다이렉트 시켜줄 주소 화이트리스트
     * ex) http://localhost:3000
     *  */
    lateinit var oAuthLogoutUrls: List<String>

    /** 지원 IdP (기본: COGNITO) */
    var supportedIdentityProviders: List<UserPoolClientIdentityProvider> =
        listOf(UserPoolClientIdentityProvider.COGNITO)

    /** 존재하지 않는 사용자 에러 숨김 (기본: true) */
    var preventUserExistenceErrors: Boolean = true

    /** 토큰 만료 기본값. 1시간 기본권장세팅 */
    var accessTokenValidity = 1.hours

    /** 억세스 따라가는게 기본권장세팅 */
    var idTokenValidity = 1.hours

    /** 30일 기본권장세팅 */
    var refreshTokenValidity = 30.days

    //==================================================== 결과: 클라이언트 ======================================================
    lateinit var userPoolClient: UserPoolClient

    fun createClient(clientBlock: UserPoolClientOptions.Builder.() -> Unit = {}): CdkCognitoClient {
        // 사전 조건: userPool 이 먼저 생성되어 있어야 함
        userPoolClient = userPool.addClient(
            "userPoolClient-${logicalName}",
            UserPoolClientOptions.builder()
                .userPoolClientName(logicalName)
                .generateSecret(generateSecret)
                .authFlows(authFlows)
                .oAuth(
                    OAuthSettings.builder()
                        .flows(oAuthFlows)
                        .scopes(oAuthScopes)
                        .callbackUrls(oAuthCallbackUrls)
                        .logoutUrls(oAuthLogoutUrls)
                        .build()
                )
                .supportedIdentityProviders(supportedIdentityProviders)
                .preventUserExistenceErrors(preventUserExistenceErrors)
                .accessTokenValidity(accessTokenValidity.toCdk())
                .idTokenValidity(idTokenValidity.toCdk())
                .refreshTokenValidity(refreshTokenValidity.toCdk())
                .apply(clientBlock)
                .build()
        )
        TagUtil.tagDefault(userPoolClient)
        return this
    }

}
