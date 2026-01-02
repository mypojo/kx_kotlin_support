package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.adminInitiateAuth
import aws.sdk.kotlin.services.cognitoidentityprovider.adminUserGlobalSignOut
import aws.sdk.kotlin.services.cognitoidentityprovider.globalSignOut
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminInitiateAuthResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminUserGlobalSignOutResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AuthFlowType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.GlobalSignOutResponse
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty


/**
 * 코그니토 백엔드용 컴포넌트 (admin IAM 권한으로 작동)
 * 챌린지 응답 (예: NEW_PASSWORD_REQUIRED 등) 생략
 * */
class CognitoSessionComponent(private val cognitoPoolId: String, private val cognitoIdClientId: String) {

    /** AwsClient 지연 주입 */
    var aws: AwsClient by LazyAwsClientProperty()

    /**
     * 백엔드 전용로직!  admin 으로 비번으로 로그인 수행
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
     * 모든 세션 로그아웃 (Global Sign-Out)
     * Cognito User Pool 내부 세션만 무효화
     * Cognito 도메인 세션 쿠키(cognito, XSRF-TOKEN 등) 유지됨
     *
     * 로그아웃 순서
     * 1. globalSignOut 호출해서 코그니토 세션 제거
     * 2. auth 도메인의 리프레시 토큰 쿠기 제거
     * 3. 코그니토 도메인의 /logout 호출해서 IdP 연동 세션 제거
     *  */
    suspend fun globalSignOut(accessToken: String): GlobalSignOutResponse = aws.cognito.globalSignOut {
        this.accessToken = accessToken
    }

    /**
     * 관리자 권한으로 특정 사용자의 모든 세션 로그아웃 (AdminUserGlobalSignOut)
     * - 유저풀 기준으로 사용자의 모든 기기 세션 무효화
     * - Cognito 도메인 세션 쿠키는 유지됨 (필요시 별도 로그아웃 처리 필요)
     */
    suspend fun adminUserGlobalSignOut(username: String): AdminUserGlobalSignOutResponse = aws.cognito.adminUserGlobalSignOut {
        this.userPoolId = cognitoPoolId
        this.username = username
    }
}

