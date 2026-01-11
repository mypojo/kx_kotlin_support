package net.kotlinx.aws.cognito

import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.*

class CognitoLoginUrlBuilder(
    /** ex) auth.domain.com */
    private val authDomain: String,
    private val clientId: String,
    private val redirectUri: String
) {

    /**
     * 로그인 버튼용 URL 생성
     * @param providerName Google 등..
     * @param state CSRF 방지를 위한 상태 값 (미지정 시 무작위 생성)
     * @param codeChallenge PKCE 적용을 위한 챌린지 값 (보안 권장)
     *
     * @see fetchTokensByCode
     */
    fun buildLoginUrl(providerName: String, state: String = UUID.randomUUID().toString().replace("-", ""), codeChallenge: String? = null): String {
        val urlBuilder = "https://$authDomain/oauth2/authorize".toHttpUrl().newBuilder()
            .addQueryParameter("identity_provider", providerName)
            .addQueryParameter("response_type", "code")
            .addQueryParameter("client_id", clientId)
            .addQueryParameter("redirect_uri", redirectUri)
            .addQueryParameter("scope", "openid profile email")
            .addQueryParameter("state", state)

        // PKCE 적용 시 파라미터 추가
        if (!codeChallenge.isNullOrBlank()) {
            urlBuilder.addQueryParameter("code_challenge", codeChallenge)
            urlBuilder.addQueryParameter("code_challenge_method", "S256")
        }

        return urlBuilder.build().toString()
    }
}
