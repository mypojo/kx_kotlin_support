package net.kotlinx.spring.security

import org.springframework.security.oauth2.jwt.NimbusJwtDecoder


/**
 * 코그니토 백엔드용 컴포넌트.
 * */
class CognitoJwtComponent(private val cognitoId: String) {


    /** 토큰 사인 키 주소. 다운로드 받아서 쓰면됨 */
    val tokenSigningKey = "https://cognito-idp.ap-northeast-2.amazonaws.com/${cognitoId}/.well-known/jwks.json"

    /**
     * 스프링 시큐리티 의존성 있음!!
     * 자체적으로 키를 캐싱해준다
     *  */
    val decoder: NimbusJwtDecoder by lazy { NimbusJwtDecoder.withJwkSetUri(tokenSigningKey).build()!! }

    // ID 토큰의 풍부한 정보를 담기 위한 확장된 데이터 클래스
    data class CognitoIdTokenInfo(
        val sub: String,
        /**
         * 구글 로그인 등에서는 넘어오지 않는다
         * */
        val username: String?,
        val email: String?,
        val isEmailVerified: Boolean,
        val phoneNumber: String?,
        val isPhoneNumberVerified: Boolean,
        val groups: List<String>,
        val roles: List<String>,
        val authTime: Long?,
        val identities: List<CognitoIdentity> = emptyList(),
        /**
         * Authentication Methods References
         * 예: ["pwd", "mfa"]
         * */
        val amr: List<String> = emptyList(),
        val iss: String? = null,
        val aud: List<String> = emptyList(),
        val exp: Long? = null,
        val iat: Long? = null,
        val jti: String? = null,
        val atHash: String? = null,
    )

    /**
     * Cognito Identity 정보
     * 소셜 로그인(구글, 애플 등) 연동 시 상세 정보를 담음
     */
    data class CognitoIdentity(
        val userId: String,
        val providerName: String,
        /**
         * Apple / Google / Facebook ..
         * */
        val providerType: String,
        val issuer: String?,
        val primary: Boolean,
        val dateCreated: String?,
    )

    /**
     * Access Token 정보를 담기 위한 데이터 클래스
     * ID 토큰보다 정보가 적으며 주로 권한(scope, groups) 확인용으로 사용됨
     * */
    data class CognitoAccessTokenInfo(
        val sub: String,
        val username: String,
        val scope: String,
        val groups: List<String>,
        val clientId: String,
        val tokenUse: String,
        val authTime: Long?,
        val iss: String? = null,
        val exp: Long? = null,
        val iat: Long? = null,
        val jti: String? = null,
    )

    /** ID 토큰 파싱 */
    fun parseIdToken(idToken: String): CognitoIdTokenInfo {
        val jwt = decoder.decode(idToken)!!
        return convertToIdTokenInfo(jwt.claims)
    }

    /** JWT claims 맵을 CognitoIdTokenInfo 객체로 변환 */
    fun convertToIdTokenInfo(claims: Map<String, Any>): CognitoIdTokenInfo {
        val identities = (claims["identities"] as? List<*>)?.mapNotNull {
            val map = it as? Map<*, *> ?: return@mapNotNull null
            CognitoIdentity(
                userId = map["userId"]?.toString() ?: "",
                providerName = map["providerName"]?.toString() ?: "",
                providerType = map["providerType"]?.toString() ?: "",
                issuer = map["issuer"]?.toString(),
                primary = map["primary"]?.toString()?.toBoolean() ?: false,
                dateCreated = map["dateCreated"]?.toString(),
            )
        } ?: emptyList()

        return CognitoIdTokenInfo(
            sub = claims["sub"]?.toString() ?: "",
            username = (claims["cognito:username"] ?: claims["username"])?.toString(),
            email = claims["email"]?.toString(),
            isEmailVerified = claims["email_verified"]?.toString()?.toBoolean() ?: false,
            phoneNumber = claims["phone_number"]?.toString(),
            isPhoneNumberVerified = claims["phone_number_verified"]?.toString()?.toBoolean() ?: false,
            groups = claims.asList("cognito:groups"),
            roles = claims.asList("cognito:roles"),
            authTime = claims.toEpochSecond("auth_time"),
            identities = identities,
            amr = claims.asList("amr"),
            iss = claims["iss"]?.toString(),
            aud = claims.asList("aud"),
            exp = claims.toEpochSecond("exp"),
            iat = claims.toEpochSecond("iat"),
            jti = claims["jti"]?.toString(),
            atHash = claims["at_hash"]?.toString(),
        )
    }

    /**
     * Access Token 파싱
     * @throws JwtValidationException
     *  */
    fun parseAccessToken(accessToken: String): CognitoAccessTokenInfo {
        val jwt = decoder.decode(accessToken)!!
        val claims = jwt.claims

        return CognitoAccessTokenInfo(
            sub = claims["sub"]?.toString() ?: "",
            username = (claims["username"] ?: claims["cognito:username"])?.toString() ?: "",
            scope = claims["scope"]?.toString() ?: "",
            groups = claims.asList("cognito:groups"),
            clientId = claims["client_id"]?.toString() ?: "",
            tokenUse = claims["token_use"]?.toString() ?: "",
            authTime = claims.toEpochSecond("auth_time"),
            iss = claims["iss"]?.toString(),
            exp = claims.toEpochSecond("exp"),
            iat = claims.toEpochSecond("iat"),
            jti = claims["jti"]?.toString(),
        )
    }

    private fun Map<String, Any>.asList(key: String): List<String> {
        return (this[key] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
    }

    /**
     * 클레임 값을 epoch second (Long)로 변환
     * Instant, Long, Number, String 등 다양한 타입을 지원
     */
    private fun Map<String, Any>.toEpochSecond(key: String): Long? {
        return when (val value = this[key]) {
            null -> null
            is java.time.Instant -> value.epochSecond
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
}

