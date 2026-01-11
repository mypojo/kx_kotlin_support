package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminCreateUserResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminGetUserResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AttributeType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.UserType
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist


val AwsClient.cognito: CognitoIdentityProviderClient
    get() = getOrCreateClient { CognitoIdentityProviderClient { awsConfig.build(this) }.regist(awsConfig) }


/**
 * 1. 속성 리스트에서 특정 키의 값을 찾는 범용 확장 함수
 */
fun List<AttributeType>?.findAttribute(name: String): String? =
    this?.find { it.name == name }?.value

/**
 * 2. UserType에서 바로 sub을 가져오는 프로퍼티
 */
val UserType.sub: String?
    get() = attributes.findAttribute("sub")

/**
 * 3. AdminCreateUserResponse에서 바로 sub을 가져오는 프로퍼티
 */
val AdminCreateUserResponse.sub: String?
    get() = user?.sub

/**
 * 4. AdminGetUserResponse에서 바로 sub을 가져오는 프로퍼티 (조회 시 사용)
 */
val AdminGetUserResponse.sub: String?
    get() = userAttributes.findAttribute("sub")