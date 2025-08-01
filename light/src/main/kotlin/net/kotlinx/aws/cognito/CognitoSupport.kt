package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.listUsers
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminGetUserRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminGetUserResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.UserType
import aws.sdk.kotlin.services.cognitoidentityprovider.paginators.listUsersPaginated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/**
 * Cognito 클라이언트
 */
val AwsClient.cognito: CognitoIdentityProviderClient
    get() = getOrCreateClient { CognitoIdentityProviderClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * 간단한 유저 정보 조회 예시
 */
suspend fun CognitoIdentityProviderClient.adminGetUser(userPoolId: String, username: String): AdminGetUserResponse {
    val request = AdminGetUserRequest {
        this.userPoolId = userPoolId
        this.username = username
    }
    return this.adminGetUser(request)
}

/** 모든 사용자 목록을 가져옵니다. */
fun CognitoIdentityProviderClient.listAllUsers(userPoolId: String): Flow<UserType> = listUsersPaginated { this.userPoolId = userPoolId }.flatMapConcat { it.users!!.asFlow() }

/** 사용자 목록을 가져옵니다. (단일 페이지) */
suspend fun CognitoIdentityProviderClient.listUsers(userPoolId: String): List<UserType> = listUsers { this.userPoolId = userPoolId }.users!!
