package net.kotlinx.aws

import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.StsAssumeRoleCredentialsProvider
import aws.sdk.kotlin.runtime.client.AwsSdkClientConfig
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProviderConfig
import aws.smithy.kotlin.runtime.http.config.HttpEngineConfig
import aws.smithy.kotlin.runtime.http.engine.HttpClientEngine
import aws.smithy.kotlin.runtime.http.engine.okhttp.OkHttpEngine
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.sfn.SfnConfig
import net.kotlinx.aws.sts.sts
import net.kotlinx.koin.Koins.koin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * AWS 설정정보
 *
 * 재시도 관련 설정은 아래 참고
 * https://docs.aws.amazon.com/sdkref/latest/guide/feature-retry-behavior.html
 *  */
data class AwsConfig(
    /**
     * patent가 없다면 로컬 프로파일로 사용 ex) projectName
     * patent가 있다면 STS ROLE 이름으로 사용  ex) ROLE_DEV
     * */
    val profileName: String? = null,
    /** AWS ID. API에 따라 필요한 경우가 있음 */
    private val inputAwsId: String? = null,
    /** 기본으로 서울 */
    val region: String = REGION_KR,

    /** http client의 타임아웃 */
    val httpConnectTimeout: Duration = 4.seconds, //디폴트 2초

    /** 람다 호출의 경우 콜드 스타트 동안 초기화가 안되서 타임아웃 날 수 있음. 따라서 넉넉히 설정 */
    val httpSocketReadTimeout: Duration = 90.seconds, //이게 디폴트임

    /** 람다 호출의 경우 콜드 스타트 동안 초기화가 안되서 타임아웃 날 수 있음. 따라서 넉넉히 설정 */
    val httpSocketWriteTimeout: Duration = 90.seconds, //이게 디폴트임
    /**
     * 쓰고난 커넥션을 풀에 얼마나 보관할지?
     * 이거 기본 60초인데, 이러면 람다 코루틴 등에서 재사용시 오류남. x초 이상 연속호출 없으면 닫게 설정
     * 서버측의 설정보다 작게 설정하면 될듯
     * */
    val connectionIdleTimeout: Duration = 2.seconds,

    /**
     * 부모가 있다면 부모를 기준으로 STS를 생성한다.
     * ex) lakeFormation 역할을 부여받아서, 해당 테이블에 접속
     *  */
    val patent: AwsConfig? = null,

    ) {

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
            val aws = koin<AwsClient>(profileName)
            val identity = runBlocking { aws.sts.getCallerIdentity() }
            log.debug { "[$profileName] AWS ID가 입력되지 않아서 STS를 통해서 로드됨 -> ${identity.account}" }
            identity.account!!
        }
    }

    /**
     * 체인 기본순서 : 환경변수 -> 프로파일
     * https://docs.aws.amazon.com/sdkref/latest/guide/settings-reference.html 에서 설정 확인
     * https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html
     *  -> DurationSeconds : 900초(15분) ~ 최대값 43200 ?? -> duration_seconds = 43200
     * */
    val credentialsProvider: CredentialsProvider = if (patent == null) {
        log.trace { "기본 환경에서 프로바이더 생성" }
        DefaultChainCredentialsProvider(
            profileName = profileName,
            region = region,
            httpClient = httpClientEngine,
        )
    } else {
        log.trace { "기본 환경의 데이터에서 STS로 프로바이더 생성" }
        StsAssumeRoleCredentialsProvider(
            bootstrapCredentialsProvider = patent.credentialsProvider,
            roleArn = "arn:aws:iam::${awsId}:role/${profileName}",
            roleSessionName = "sts", //공백문자 허용안함
            region = region
        )
    }

    /**
     * STS를 사용해서 새로운 연결은 만든다
     * ex) 중앙계정 lakeformation 접근
     *  */
    fun toSts(awsId: String, role: String): AwsConfig = AwsConfig(
        inputAwsId = awsId,
        profileName = role,
        patent = this
    )

    //==================================================== client 빌드 옵션 추가 ======================================================

    /** 빌더에 각종 설정을 추가해줌 */
    fun build(clientBuilder: Any) {
        //이제 credentialsProvider 에서 설정했기 때문에엔진하고 리전은 필요 없을지도?
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

        /** 한국-서울 */
        const val REGION_KR: String = "ap-northeast-2"

        /** 북미서버 메인 (인증서 등록 등) */
        const val REGION_US: String = "us-east-1"

        /**
         * 간단 계산용 환율
         * 뉴 노멀 적용
         *  */
        const val EXCHANGE_RATE: Int = 1400

    }
}