package net.kotlinx.awscdk.data

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream.*
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStreamProps
import software.amazon.awscdk.services.s3.IBucket

/**
 * 아이스버그 버전이 더 좋은데, 이벤트브릿지의 이벤트는 직접  라우팅하는게 불가능. 이때문에 이거도 수요가 있긴함
 *
 * #1 레이크포메이션 사용하는경우 IAM 말고 레이크포메이션 콘솔에서 role에 권한 줘야함 (Data lake administrators 지정 등등)
 * #2 바닐라 파케이라서 , 따로 파티셔닝을 잡아줘야함 (귀찮음!!)
 *
 * 파이어호스 사용법
 * #1 직접 put 하기 -> Athena 테이블 샘플은 HttpLogTable 참고
 * #2 이벤트 브릿지로 쏜 다음, 리스너 걸어서 연결하기 -> Athena 테이블 샘플은 EventBridgeTable 참고
 * @see CdkFirehoseToIceberg
 *  */
class CdkFirehoseToS3Parquet : CdkInterface {

    @Kdsl
    constructor(block: CdkFirehoseToS3Parquet.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "${streamName}-${suff}"

    /** KDF 전송 역할 */
    lateinit var role: IRole

    /** 아테나 테이블의 데이터베이스 */
    lateinit var databaseName: String

    /** 아테나 테이블 네임 */
    lateinit var streamName: String

    /** S3저장 버킷 */
    lateinit var bucket: IBucket

    /**
     * 저장될 S3 경로.
     * ex) data/${tableName}/basic_date=!{partitionKeyFromQuery:basic_date}/name=!{partitionKeyFromQuery:name}/
     * ex) data/${tableName}/basicDate=!{timestamp:yyyyMMdd}/hh=!{timestamp:HH}/
     * */
    lateinit var prefix: String

    /**
     * 에러 경로
     * ex) data/level1/${streamName}/!{firehose:error-output-type}/basicDate=!{timestamp:yyyyMMdd}/
     * */
    lateinit var errorOutputPrefix: String

    /**
     * S3 경로에 들어갈 값 추출형식
     *  ex) {basic_date:.basic_date,name:.name}
     * */
    lateinit var prefixParameterValue: String

    /**
     * 실서버는 10분에 한번 로깅.
     * 최초 개발시 빠른 반응을 위해서 60(최소치)으로 하자.
     * */
    var intervalInSeconds = if (deploymentType == DeploymentType.PROD) 60 * 10 else 60

    /** 결과물 */
    lateinit var deliveryStream: CfnDeliveryStream

    fun create(stack: Stack) {
        deliveryStream = CfnDeliveryStream(
            stack, "kdf-$logicalName", CfnDeliveryStreamProps.builder()
                .deliveryStreamName(logicalName)
                .deliveryStreamType("DirectPut")
                /** s3DestinationConfiguration 가 아닌 확장을 사용. 순서는 AWS 콘솔의 JSON 기준 */
                .extendedS3DestinationConfiguration(
                    ExtendedS3DestinationConfigurationProperty.builder()
                        .bucketArn(bucket.bucketArn)
                        .roleArn(role.roleArn)
                        //버퍼 용량 128은 거의 고정 권장
                        .bufferingHints(BufferingHintsProperty.builder().sizeInMBs(128).intervalInSeconds(intervalInSeconds).build())
                        //파티션 프로세서
                        .processingConfiguration(
                            ProcessingConfigurationProperty.builder().enabled(true)
                                .processors(
                                    listOf(
                                        ProcessorProperty.builder().type("MetadataExtraction")
                                            .parameters(
                                                listOf(
                                                    ProcessorParameterProperty.builder().parameterName("MetadataExtractionQuery").parameterValue(prefixParameterValue).build(),
                                                    ProcessorParameterProperty.builder().parameterName("JsonParsingEngine").parameterValue("JQ-1.6").build(),
                                                )
                                            )
                                            .build()
                                    )
                                )
                                .build()
                        )
                        .prefix(prefix)
                        .errorOutputPrefix(errorOutputPrefix)
                        .compressionFormat("UNCOMPRESSED")  //must be set to UNCOMPRESSED when data format conversion is enabled -> 실제는 스내피로 적용될듯
                        .cloudWatchLoggingOptions(
                            CloudWatchLoggingOptionsProperty.builder()
                                .enabled(true)
                                .logGroupName("/aws/kinesisfirehose/${logicalName}")
                                .logStreamName("DestinationDelivery") //변경 가능한지 모르겠음
                                .build()
                        )
                        .encryptionConfiguration(EncryptionConfigurationProperty.builder().noEncryptionConfig("NoEncryption").build()) //S3의 기본 암호화만 적용함
                        .s3BackupMode("Disabled") //백업 안함
                        .fileExtension("") //먼지 모르겠음
                        .customTimeZone("Asia/Seoul") //이래야 한국 시간/날짜로 파티셔닝됨

                        .dataFormatConversionConfiguration(
                            DataFormatConversionConfigurationProperty.builder()
                                .enabled(true)
                                .inputFormatConfiguration(INPUT_FORMAT)
                                .outputFormatConfiguration(OUTPUT_FORMAT)
                                .schemaConfiguration(
                                    SchemaConfigurationProperty.builder()
                                        .databaseName(databaseName)
                                        .region(awsConfig.region)
                                        .roleArn(role.roleArn)
                                        .tableName(streamName)
                                        .versionId("LATEST")
                                        .build()
                                )
                                .build()
                        )

                        //S3 경로에는 대소문자가 구분됨. 하지만 헷갈리니 언더스코어로 하자.
                        //하나의 인프라로 모든 어플리케이션이 다 사용하려면 다이나믹 파티션이 이썽야함
                        .dynamicPartitioningConfiguration(
                            DynamicPartitioningConfigurationProperty.builder()
                                .enabled(true)
                                .retryOptions(RetryOptionsProperty.builder().durationInSeconds(300).build())
                                .build()
                        )


                        .build()
                )
                .build()
        )
        TagUtil.tagDefault(deliveryStream)
    }

    companion object {
        val INPUT_FORMAT: InputFormatConfigurationProperty =
            InputFormatConfigurationProperty.builder().deserializer(DeserializerProperty.builder().openXJsonSerDe(OpenXJsonSerDeProperty.builder().build()).build()).build()!!
        val OUTPUT_FORMAT: OutputFormatConfigurationProperty =
            OutputFormatConfigurationProperty.builder().serializer(SerializerProperty.builder().parquetSerDe(ParquetSerDeProperty.builder().build()).build()).build()!!
    }
}