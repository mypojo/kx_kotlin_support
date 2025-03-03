package net.kotlinx.awscdk.basic

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.logs.CfnLogAnomalyDetector
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.LogGroupProps
import software.amazon.awscdk.services.logs.RetentionDays

class CdkLogGroup : CdkInterface {

    @Kdsl
    constructor(block: CdkLogGroup.() -> Unit = {}) {
        apply(block)
    }

    /** VPC 이름 */
    override val logicalName: String
        get() = "/aws/${serviceName}-${suff}"

    /**
     * 서비스명.  이걸로 로그 패스가 결정됨
     * ex) ecs/job
     *  */
    lateinit var serviceName: String

    /** 로그 보관 주기 */
    var retentionDays: RetentionDays = RetentionDays.FIVE_YEARS

    /** 결과 */
    lateinit var logGroup: LogGroup


    /**
     * 기본적으로 grok 이라고 간주
     * 아래에서 사용 가능한 패턴 찾을 수 있음
     * https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/CloudWatch-Logs-Transformation-Processors.html#Grok-Patterns
     *
     * !!! 2025 02 기준 아직 CDK로는 발매되지 않은듯함!
     *
     * 최대 5개 까지만 지원함으로 아래처럼 간략화 해야함
     * %{DATA:header} [%{INT:eventId}/%{INT:loginId}/%{INT:userId}] %{GREEDYDATA:message}
     * */
    var transformerMatch: String? = null

    /**
     * 트랜스포머 만들고 여기로 인덱스 달면 원하는 데이터를  Logs Insights 로 빠르게 조회 가능
     * ex)  filter eventId = 77117819
     * */
    var fieldIndexs: List<String> = emptyList()

    /** 테스트 CDK 생성시 DESTROY로 해야 오류가 안남 */
    fun create(stack: Stack) {
        logGroup = LogGroup(
            stack, logicalName, LogGroupProps.builder()
                .logGroupName(logicalName)
                .retention(retentionDays)
                .removalPolicy(RemovalPolicy.DESTROY) //편의상 DESTROY
                .build()
        )

    }

    /**
     * 아노말리 디텍터 추가
     * 기본적으로 공짜로 작동함
     * 디폴트로 30분..
     *  */
    fun addLogAnomalyDetector(stack: Stack, block: CfnLogAnomalyDetector.Builder.() -> Unit = {}) {
        CfnLogAnomalyDetector.Builder.create(stack, "${logicalName}-anomaly_detector")
            .detectorName("${logicalName}-anomaly_detector")
            .logGroupArnList(listOf(logGroup.logGroupArn)) //1개밖에 안됨.. 이때문에 1:1 매핑
            .evaluationFrequency("THIRTY_MIN") //FIVE_MIN | TEN_MIN | FIFTEEN_MIN | THIRTY_MIN | ONE_HOUR
            .anomalyVisibilityTime(14) //days  임!
            .apply(block)
            .build()
    }

}