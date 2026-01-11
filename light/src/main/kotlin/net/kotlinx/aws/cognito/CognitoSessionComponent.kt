package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.adminInitiateAuth
import aws.sdk.kotlin.services.cognitoidentityprovider.adminUserGlobalSignOut
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminInitiateAuthResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminUserGlobalSignOutResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AuthFlowType
import aws.sdk.kotlin.services.cognitoidentityprovider.revokeToken
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.okhttp.OkHttpMediaType
import net.kotlinx.okhttp.await
import okhttp3.FormBody
import okhttp3.OkHttpClient


/**
 * 코그니토 백엔드용 컴포넌트 (admin IAM 권한으로 작동)
 * 챌린지 응답 (예: NEW_PASSWORD_REQUIRED 등) 생략
 * */
class CognitoSessionComponent(
    val cognitoPoolId: String,
    val cognitoIdClientId: String,
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /** AwsClient 지연 주입 */
    var aws: AwsClient by LazyAwsClientProperty()

    private val client = koin<OkHttpClient>()

    /**
     * Cognito OAuth2 토큰 응답 데이터 객체
     */
    data class CognitoTokenResponse(
        val id_token: String? = null,
        val access_token: String? = null,
        val refresh_token: String? = null,
        val expires_in: Int? = null,
        val token_type: String? = null,
    )

    /**
     * 구글 로그인 등을 위한 OAuth2 토큰 획득 (OkHttp await 사용)
     * @param domain Cognito 사용자 풀의 도메인 (예: https://your-domain.auth.ap-northeast-2.amazoncognito.com)
     * @param code 인가 코드
     * @param redirectUri 등록된 리다이렉트 URI (authorization code를 발급받을 때 사용한 엔드포인트와 정확히 동일해야 한다)
     * @param codeVerifier PKCE code_verifier (nullable)
     *
     * @see CognitoLoginUrlBuilder
     */
    suspend fun fetchTokensByCode(domain: String, code: String, redirectUri: String, codeVerifier: String? = null): CognitoTokenResponse {
        val url = if (domain.endsWith("/")) "${domain}oauth2/token" else "$domain/oauth2/token"
        log.debug { "Fetching tokens from Cognito domain: $domain" }
        val resp = client.await {
            this.url = url
            this.method = "POST"
            this.mediaType = OkHttpMediaType.FORM_URLENCODED
            this.body = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", cognitoIdClientId)
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .apply {
                    if (!codeVerifier.isNullOrBlank()) {
                        add("code_verifier", codeVerifier)
                    }
                }
                .build()
        }

        if (!resp.ok) {
            throw IllegalStateException("Failed to fetch tokens: ${resp.response.code} ${resp.respText}")
        }

        return GsonData.parse(resp.respText).fromJson()
    }

    /**
     * 백엔드 전용로직!  admin 으로 비번으로 로그인 수행
     * 주의!! 관리자 권한이지만 비번 있어야함!
     * @param loginIdentifier  이메일(email)이나 전화번호(phone_number) 도 가능함
     * */
    suspend fun initiateAuth(loginIdentifier: String, password: String): AdminInitiateAuthResponse = aws.cognito.adminInitiateAuth {
        this.userPoolId = cognitoPoolId
        this.clientId = cognitoIdClientId
        this.authFlow = AuthFlowType.AdminNoSrpAuth  //현재는 `ADMIN_NO_SRP_AUTH`가 표준으로 사용 (Backend 전용)
        this.authParameters = mapOf(
            "USERNAME" to loginIdentifier,
            "PASSWORD" to password,
        )
    }

    /**
     * 토큰 재발급 패턴: REFRESH_TOKEN_AUTH
     * 리프레시 토큰으로 억세스 토큰 재발급
     *  */
    suspend fun refreshTokens(refreshToken: String): AdminInitiateAuthResponse = aws.cognito.adminInitiateAuth {
        this.userPoolId = cognitoPoolId
        this.clientId = cognitoIdClientId
        this.authFlow = AuthFlowType.RefreshTokenAuth
        this.authParameters = mapOf(
            "REFRESH_TOKEN" to refreshToken,
        )
    }

    /**
     * 일반적인 로그아웃
     * refreshToken 만 무효화 시킨다
     * */
    suspend fun signOut(refreshToken: String) {
        aws.cognito.revokeToken {
            this.clientId = cognitoIdClientId
            this.token = refreshToken // 만약 Client Secret을 사용 중이라면 secretHash 계산 로직이 추가되어야 합니다.
        }
    }

    /**
     * 관리자 권한으로 특정 Cognito 사용자의 모든 인증 세션을 강제 종료한다.
     *
     * - User Pool 기준으로 해당 사용자의 모든 Refresh Token을 즉시 무효화한다.
     *   (웹/앱/모든 기기에서 토큰 재발급 불가)
     * - 이미 발급된 Access / ID Token은 만료 전까지 유효하다.
     * - Cognito는 브라우저 쿠키나 Hosted UI 세션을 직접 제거하지 않는다.
     *
     * 로그아웃 후 추가 처리 (필요 시):
     * 1. auth 도메인의 Refresh Token 쿠키 제거
     * 2. Cognito 도메인의 /logout 호출 (Hosted UI / Google IdP 자동 로그인 방지 목적)
     */
    suspend fun signOutAll(username: String): AdminUserGlobalSignOutResponse = aws.cognito.adminUserGlobalSignOut {
        this.userPoolId = cognitoPoolId
        this.username = username
    }
}

