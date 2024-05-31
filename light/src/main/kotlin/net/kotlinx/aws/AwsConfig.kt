package net.kotlinx.aws

import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.sdk.kotlin.runtime.client.AwsSdkClientConfig
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProviderConfig
import aws.smithy.kotlin.runtime.http.config.HttpEngineConfig
import aws.smithy.kotlin.runtime.http.engine.HttpClientEngine
import aws.smithy.kotlin.runtime.http.engine.okhttp.OkHttpEngine
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.sfn.SfnConfig
import net.kotlinx.koin.Koins.koin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/** AWS 설정정보 */
data class AwsConfig(
    /** 프로필 없으면 환경변서(체인 순서대로) 적용 */
    val profileName: String? = null,
    /** AWS ID. API에 따라 필요한 경우가 있음 */
    private val inputAwsId: String? = null,
    /** 기본으로 서울 */
    val region: String = SEOUL,

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
        this.connectTimeout = httpConnectTimeout
        this.socketReadTimeout = httpSocketReadTimeout
        this.socketWriteTimeout = httpSocketWriteTimeout
        this.connectionIdleTimeout = this@AwsConfig.connectionIdleTimeout
    }

    /**
     * AWS ID
     * 최초 입력시 누락되었다면 늦은 로딩한다.
     *  */
    val awsId: String by lazy {
        inputAwsId ?: run {
            val aws = koin<AwsClient1>(profileName)
            val identity = runBlocking { aws.sts.getCallerIdentity() }
            log.debug { "[$profileName] AWS ID가 입력되지 않아서 STS를 통해서 로드됨 -> ${identity.account}" }
            identity.account!!
        }
    }

    //==================================================== client 빌드 옵션 추가 ======================================================

    /** 빌더에 각종 설정을 추가해줌 */
    fun build(clientBuilder: Any) {
        if (clientBuilder is AwsSdkClientConfig.Builder) clientBuilder.region = region
        if (clientBuilder is CredentialsProviderConfig.Builder) clientBuilder.credentialsProvider = credentialsProvider
        if (clientBuilder is HttpEngineConfig.Builder) clientBuilder.httpClient = httpClientEngine
    }

    //==================================================== 제품별 설정  ======================================================

    /** SFN 설정 */
    val sfnConfig: SfnConfig by lazy { SfnConfig(this) }

    /**
     * ECR 경로
     * 이 위에 버전 정보가 붙을 수 있다.
     * ex) 331671628331.dkr.ecr.ap-northeast-2.amazonaws.com/media-data:latest
     * ex) 331671628331.dkr.ecr.ap-northeast-2.amazonaws.com/media-data@sha256:24c4d31fc292a57f32ffcd4d2719f0b10bfea9d08786af589196547af5bb960f
     */
    fun ecrPath(repositoryName: String): String = "${awsId}.dkr.ecr.${region}.amazonaws.com/${repositoryName}"


    companion object {

        private val log = KotlinLogging.logger {}

        const val SEOUL = "ap-northeast-2"
    }
}