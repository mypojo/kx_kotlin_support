package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.associateSoftwareToken
import aws.sdk.kotlin.services.cognitoidentityprovider.model.VerifySoftwareTokenResponseType
import aws.sdk.kotlin.services.cognitoidentityprovider.verifySoftwareToken
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty

/**
 * Cognito MFA (Multi-Factor Authentication) 관리를 위한 컴포넌트
 */
class CognitoMfaComponent(
    /**
     * MFA 등록 시 사용한 이름 (예: AWS)
     * 한글 가능!
     *  */
    val issuer: String,
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /** AwsClient 지연 주입 */
    var aws: AwsClient by LazyAwsClientProperty()

    /**
     * MFA 소프트웨어 토큰 연결 시작 (Secret Code 생성) 후 OTP URI 반환
     * otpauth://totp/{Issuer}:{User}?secret={SecretCode}&issuer={Issuer}
     *
     * 주의!
     * 내부적으로는 MFA를 1개만 등록가능
     * 신규로 secretCode 발급 후 verifySoftwareToken 로 첫 인증하는순간 오버라이드됨
     *
     * @param accessToken 액세스 토큰
     * @param userIdentifier 사용자 식별자 (예: 이메일 주소, 핸드폰 번호)
     * @return OTP 등록용 URI
     */
    suspend fun associateSoftwareToken(accessToken: String, userIdentifier: String): String {
        log.debug { "Associating software token for $userIdentifier" }
        val resp = aws.cognito.associateSoftwareToken {
            this.accessToken = accessToken
        }
        val secretCode = resp.secretCode ?: throw IllegalStateException("Secret code is null")
        return "otpauth://totp/$issuer:$userIdentifier?secret=$secretCode&issuer=$issuer"
    }

    /**
     * 소프트웨어 토큰 검증 및 등록 확정
     * @param accessToken 액세스 토큰
     * @param userCode 사용자가 입력한 6자리 번호
     */
    suspend fun verifySoftwareToken(accessToken: String, userCode: String): Boolean {
        log.debug { "Verify software token with code: $userCode" }
        val resp = aws.cognito.verifySoftwareToken {
            this.accessToken = accessToken
            this.userCode = userCode
        }
        return resp.status == VerifySoftwareTokenResponseType.Success
    }

}
