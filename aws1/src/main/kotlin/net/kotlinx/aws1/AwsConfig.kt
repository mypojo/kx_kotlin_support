package net.kotlinx.aws1

import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider

/** AWS 설정정보 */
data class AwsConfig(
    /** 프로필 없으면 환경변서(체인 순서대로) 적용 */
    val profileName: String? = null,
    /** 기본으로 서울 */
    val region: String = "ap-northeast-2"
) {
    /**
     * 우선순위 : 프로파일 -> 환경변수(체인 기본순서)
     * */
    val credentialsProvider: CredentialsProvider? = profileName?.let { DefaultChainCredentialsProvider(profileName = profileName) }
}