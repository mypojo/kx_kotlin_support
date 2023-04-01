package net.kotlinx.aws_cdk.module

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.KdfUtil
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core1.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.*
import software.amazon.awscdk.services.events.targets.KinesisFirehoseStream
import software.amazon.awscdk.services.events.targets.KinesisFirehoseStreamProps
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream.BufferingHintsProperty
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream.EncryptionConfigurationProperty
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStreamProps
import software.amazon.awscdk.services.s3.IBucket

/** 나중에 작업. */
enum class EventLogType {
    /** GZIP으로 변환 */
    GZIP,

    /** 파케이로 바로 변환.  */
    PARQUET,
}


/**
 * 이벤트버스로 이벤트를 던지면 KDF 로 전송되어 athena 테이블위치로 데이터가 기록됨
 *  */
class EventLogModule(
    val project: CdkProject,
    val eventBus: IEventBus,
    val bucket: IBucket,
    val role: IRole,
    val deploymentType: DeploymentType,
    val tablePrefix: String = "event1_",
) {

    /**
     * KDF 생성 & 특정 이벤트를 KDF로 보내주는 rule을 생성한다.
     * 아테나 테이블 컬럼명은 소문자로 리플레이스 된다. 어디까지 변환되는지 헷갈리니 언더바 형식을 사용할것
     * */
    fun create(stack: Stack, eventName: String, eventPattern: EventPattern): String {
        val compressionFormat = if (deploymentType == DeploymentType.prod) "GZIP" else "UNCOMPRESSED" // => 개발은 "UNCOMPRESSED"
        val tableName = "${tablePrefix}${eventName}" //전체 프로젝트 공통임으로 프로젝트 이름이 들어가지 않는다.
        val intervalInSeconds = if (deploymentType == DeploymentType.prod) 60 * 10 else 60 //실서버는 10분에 한번 로깅. 60이 최소. 최초 개발시 빠른 반응을 위해서 60으로 하자.
        val deliveryStreamName = "${tableName}-${deploymentType}"


        val awsEventLogRuleName = "${project.projectName}-event_${eventName}-${deploymentType}"
        //CDK로 답이 없음. 오버라이드라도 하게 해주지.. 그냥 콘솔에서 \n 추가 (엔터키 누르기)
        val message: RuleTargetInput? = RuleTargetInput.fromObject("");
//        val message: RuleTargetInput = RuleTargetInput.fromObject({
//            "id": EventField.fromPath("$.id"),
//            "detail-type": EventField.fromPath("$.detail-type"),
//            "source": EventField.fromPath("$.source"),
//            "account": EventField.fromPath("$.account"),
//            "time": EventField.fromPath("$.time"),
//            "region": EventField.fromPath("$.region"),
//            "detail": EventField.fromPath("$.detail"),
//        })
        //실제 샘플
        val inputPath = """
        {
            "account": "${'$'}.account",
            "detail": "${'$'}.detail",
            "detail-type": "${'$'}.detail-type",
            "id": "${'$'}.id",
            "region": "${'$'}.region",
            "source": "${'$'}.source",
            "time": "${'$'}.time"
        }            
        """.trimIndent()
        val inputTemplate = "{\"id\":<id>,\"detail-type\":<detail-type>,\"source\":<source>,\"account\":<account>,\"time\":<time>,\"region\":<region>,\"detail\":<detail>}\n" //마지막에 개행 한줄 있음


        val firehoseDeliveryStream = CfnDeliveryStream(
            stack, "kdf-$deliveryStreamName", CfnDeliveryStreamProps.builder()
                .deliveryStreamName(deliveryStreamName)
                .deliveryStreamType("DirectPut")
                .s3DestinationConfiguration(
                    CfnDeliveryStream.S3DestinationConfigurationProperty.builder()
                        .bucketArn(bucket.bucketArn)
                        .bufferingHints(BufferingHintsProperty.builder().sizeInMBs(KdfUtil.KDF_MAX_MB).intervalInSeconds(intervalInSeconds).build())
                        .compressionFormat(compressionFormat)  //UNCOMPRESSED | GZIP | ZIP | Snappy | HADOOP_SNAPPY
                        .encryptionConfiguration(EncryptionConfigurationProperty.builder().noEncryptionConfig("NoEncryption").build()) //암호화 안함
                        .roleArn(role.roleArn)
                        //S3 경로에는 대소문자가 구분됨. 하지만 헷갈리니 언더스코어로 하자.
                        .prefix("firehose/${tableName}/basicDate=!{timestamp:yyyyMMdd}/hh=!{timestamp:HH}/")
                        .errorOutputPrefix("firehose/${tableName}_!{firehose:error-output-type}/basicDate=!{timestamp:yyyyMMdd}/hh=!{timestamp:HH}/")
                        .build()
                )
                .build()
        )

        val awsEventLogRule = Rule(
            stack, awsEventLogRuleName, RuleProps.builder()
                .enabled(true)
                .ruleName(awsEventLogRuleName)
                .description("AWS event to LogGroup - $deploymentType")
                .eventBus(eventBus)
                .eventPattern(eventPattern)
                .targets(
                    listOf(
                        KinesisFirehoseStream(firehoseDeliveryStream, KinesisFirehoseStreamProps.builder().message(message).build())
                    )
                )
                .build()
        )
        TagUtil.tag(awsEventLogRule, deploymentType)
        return tableName
    }


}