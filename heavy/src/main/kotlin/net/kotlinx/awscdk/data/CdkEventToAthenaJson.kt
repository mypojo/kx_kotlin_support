package net.kotlinx.awscdk.data

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import net.kotlinx.system.DeploymentType
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

    /** 파케이로 바로 변환(추천) */
    PARQUET,
}


/**
 * 이벤트버스로 이벤트를 던지면 KDF 로 전송되어 athena 테이블위치로 데이터가 기록됨
 * ex) AWS 특정 이벤트를 모아서 athena 쿼리로 조회하고싶음
 * ex) RDB 변경 내역을 athena 리로 조회하고싶음
 *  */
class EventLogModule : CdkInterface {

    @Kdsl
    constructor(block: EventLogModule.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "${project.projectName}-event_${eventName}-${deploymentType.name.lowercase()}"

    /**
     * 이벤트명.
     * ex) aws, web, job ..
     *  */
    lateinit var eventName: String

    /** 이벤트버스 */
    lateinit var eventBus: IEventBus

    /** S3 버킷 */
    lateinit var bucket: IBucket

    /** 트리거 역할 */
    lateinit var role: IRole

    /**
     * athena 테이블명. 보통 프로젝트 공통으로 사용함
     * ex) event1_${eventName}
     *  */
    lateinit var tableName: String

    /**
     * 지정된 이벤트 패턴
     * 보통 특정 source or detailType 전체 사용
     *  */
    lateinit var eventPattern: EventPattern

    /**
     * KDF 생성 & 특정 이벤트를 KDF로 보내주는 rule을 생성한다.
     * 이벤트브릿지 -> KDF -> S3 (테이블은 수동 구성해야함)
     * 아테나 테이블 컬럼명은 소문자로 리플레이스 된다. 어디까지 변환되는지 헷갈리니 언더바 형식을 사용할것
     * */
    fun create(stack: Stack): String {
        val compressionFormat = if (deploymentType == DeploymentType.PROD) "GZIP" else "UNCOMPRESSED" // => 개발은 "UNCOMPRESSED"
        val intervalInSeconds = if (deploymentType == DeploymentType.PROD) 60 * 10 else 60 //실서버는 10분에 한번 로깅. 60이 최소. 최초 개발시 빠른 반응을 위해서 60으로 하자.
        val deliveryStreamName = "${tableName}-${deploymentType.name.lowercase()}"

        //CDK로 답이 없음. 오버라이드라도 하게 해주지.. 그냥 콘솔에서 \n 추가?? (엔터키 누르기)
        val message: RuleTargetInput = RuleTargetInput.fromObject(
            mapOf(
                "id" to EventField.fromPath("$.id"),
                "detail-type" to EventField.fromPath("$.detail-type"),
                "source" to EventField.fromPath("$.source"),
                "account" to EventField.fromPath("$.account"),
                "time" to EventField.fromPath("$.time"),
                "region" to EventField.fromPath("$.region"),
                "detail" to EventField.fromPath("$.detail"),
            )
        )

        /** 이걸로 되나? */
        val inputTemplate =
            "{\"id\":<id>,\"detail-type\":<detail-type>,\"source\":<source>,\"account\":<account>,\"time\":<time>,\"region\":<region>,\"detail\":<detail>}\n" //마지막에 개행 한줄 있음

        val deliveryStream = CfnDeliveryStream(
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
        TagUtil.tag(deliveryStream, deploymentType)

        val eventToKinesisRule = Rule(
            stack, logicalName, RuleProps.builder()
                .enabled(true)
                .ruleName(logicalName)
                .description("AWS event to LogGroup - $deploymentType")
                .eventBus(eventBus)
                .eventPattern(eventPattern)
                .targets(
                    listOf(
                        //KinesisFirehoseStreamV2(deliveryStream,KinesisFirehoseStreamProps.builder().message(message).build()), //이거 방법을 찾기 힘들어서 스킵
                        KinesisFirehoseStream(deliveryStream, KinesisFirehoseStreamProps.builder().message(message).build()),
                    )
                )
                .build()
        )
        TagUtil.tag(eventToKinesisRule, deploymentType)
        return tableName
    }


}