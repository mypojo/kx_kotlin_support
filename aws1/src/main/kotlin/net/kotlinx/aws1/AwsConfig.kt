package net.kotlinx.aws1

import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.http.engine.HttpClientEngine
import aws.smithy.kotlin.runtime.http.engine.okhttp.OkHttpEngine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** AWS 설정정보 */
data class AwsConfig(
    /** 프로필 없으면 환경변서(체인 순서대로) 적용 */
    val profileName: String? = null,
    /** 기본으로 서울 */
    val region: String = "ap-northeast-2",

    /** http client의 커넥션 */
    val httpMaxConnections: Int = 16, //디폴투 그대로
    /** http client의 타임아웃 */
    val httpConnectTimeout: Duration = 4.seconds, //디폴트 2초


    ) {
    /**
     * 우선순위 : 프로파일 -> 환경변수(체인 기본순서)
     * */
    val credentialsProvider: CredentialsProvider? = profileName?.let { DefaultChainCredentialsProvider(profileName = profileName) }

    /** SDK 기본 클라이언트는 버려진듯. 근데 이러면 클라이언트가 어려개 생기지 않는지? */
    val httpClientEngine:HttpClientEngine = OkHttpEngine {
        this.maxConnections = httpMaxConnections.toUInt()
        this.connectTimeout = httpConnectTimeout
    }
}