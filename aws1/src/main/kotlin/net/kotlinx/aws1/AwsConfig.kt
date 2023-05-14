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
    /** AWS ID. API에 따라 필요한 경우가 있음 */
    val awsId: String? = null,
    /** 기본으로 서울 */
    val region: String = SEOUL,

    /** http client의 커넥션 */
    val httpMaxConnections: Int = 16, //디폴투 그대로
    /** http client의 타임아웃 */
    val httpConnectTimeout: Duration = 4.seconds, //디폴트 2초

    val httpSocketReadTimeout: Duration = 30.seconds, //이게 디폴트임

    val httpSocketWriteTimeout: Duration = 30.seconds, //이게 디폴트임
    /**
     * 쓰고난 커넥션을 풀에 얼마나 보관할지?
     * 이거 기본 60초인데, 이러면 람다 코루틴 등에서 재사용시 오류남. x초 이상 연속호출 없으면 닫게 설정
     * 서버측의 설정보다 작게 설정하면 될듯
     * */
    val connectionIdleTimeout: Duration = 2.seconds,

    ) {
    /**
     * 체인 기본순서 : 환경변수 -> 프로파일
     * https://docs.aws.amazon.com/sdkref/latest/guide/settings-reference.html 에서 설정 확인
     * https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html
     *  -> DurationSeconds : 900초(15분) ~ 최대값 43200 ?? -> duration_seconds = 43200
     * */
    val credentialsProvider: CredentialsProvider? = profileName?.let { DefaultChainCredentialsProvider(profileName = profileName) }

    /** SDK 기본 클라이언트는 버려진듯. 근데 이러면 클라이언트가 어려개 생기지 않는지? */
    val httpClientEngine: HttpClientEngine = OkHttpEngine {
        this.maxConnections = httpMaxConnections.toUInt()
        this.connectTimeout = httpConnectTimeout
        this.socketReadTimeout = httpSocketReadTimeout
        this.socketWriteTimeout = httpSocketWriteTimeout
        this.connectionIdleTimeout = this@AwsConfig.connectionIdleTimeout
    }

    companion object {
        const val SEOUL = "ap-northeast-2"
    }
}