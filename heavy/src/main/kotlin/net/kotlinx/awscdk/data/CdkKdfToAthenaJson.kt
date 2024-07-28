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
 * 직접 KDF에 JSON을 입력하면 테이블에 데이터를 쌓아주는 KDF 스트림
 * ex) 특정 API 사용마다 비용을 kinesis로 전송 -> athena로 비용 분석
 * 암호화의 경우 확인 필요
 *  */
class CdkKdfToAthenaJson : CdkInterface {

    @Kdsl
    constructor(block: CdkKdfToAthenaJson.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "${tableName}-${deploymentType.name.lowercase()}"

    /** KDF 전송 역할 */
    lateinit var role: IRole

    /** 아테나 테이블의 데이터베이스 */
    var databaseName: String = project.profileName!!.substring(1)

    /** 아테나 테이블 네임 */
    lateinit var tableName: String

    /** S3저장 버킷 */
    lateinit var bucket: IBucket

    /**
     * 저장될 S3 경로.
     * ex) data/${tableName}/basic_date=!{partitionKeyFromQuery:basic_date}/name=!{partitionKeyFromQuery:name}/
     * */
    lateinit var prefix: String

    /**
     * S3 경로에 들어갈 값 추출형식
     *  ex) {basic_date:.basic_date,name:.name}
     * */
    lateinit var prefixParameterValue: String

    fun create(stack: Stack) {
        val intervalInSeconds = if (deploymentType == DeploymentType.PROD) 60 * 10 else 60 //실서버는 10분에 한번 로깅. 60이 최소. 최초 개발시 빠른 반응을 위해서 60으로 하자.
        val deliveryStream = CfnDeliveryStream(
            stack, "kdf-$logicalName", CfnDeliveryStreamProps.builder()
                .deliveryStreamName(logicalName)
                .deliveryStreamType("DirectPut")
                /** s3DestinationConfiguration 가 아닌 확장을 사용 */
                .extendedS3DestinationConfiguration(
                    ExtendedS3DestinationConfigurationProperty.builder()
                        .bucketArn(bucket.bucketArn)
                        .bufferingHints(BufferingHintsProperty.builder().sizeInMBs(10).intervalInSeconds(intervalInSeconds).build())
                        .compressionFormat("UNCOMPRESSED")  //must be set to UNCOMPRESSED when data format conversion is enabled -> 실제는 스내피로 적용될듯
                        .encryptionConfiguration(EncryptionConfigurationProperty.builder().noEncryptionConfig("NoEncryption").build()) //암호화 안함
                        .roleArn(role.roleArn)
                        //S3 경로에는 대소문자가 구분됨. 하지만 헷갈리니 언더스코어로 하자.
                        .prefix(prefix)
                        .errorOutputPrefix("data/${tableName}/errors/")
                        .dataFormatConversionConfiguration(
                            DataFormatConversionConfigurationProperty.builder()
                                .enabled(true)
                                .inputFormatConfiguration(INPUT_FORMAT)
                                .outputFormatConfiguration(OUTPUT_FORMAT)
                                .schemaConfiguration(
                                    SchemaConfigurationProperty.builder()
                                        .databaseName(databaseName)
                                        .region(project.region)
                                        .roleArn(role.roleArn)
                                        .tableName(tableName)
                                        .versionId("LATEST")
                                        .build()
                                )
                                .build()
                        )
                        .dynamicPartitioningConfiguration(
                            DynamicPartitioningConfigurationProperty.builder()
                                .enabled(true)
                                .retryOptions(RetryOptionsProperty.builder().durationInSeconds(300).build())
                                .build() //확인하지 못한 옵션
                        )
                        .processingConfiguration(
                            ProcessingConfigurationProperty.builder().enabled(true)
                                .processors(
                                    listOf(
                                        ProcessorProperty.builder().type("AppendDelimiterToRecord")
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
                        .build()
                )
                .build()
        )
        TagUtil.tag(deliveryStream, deploymentType)
    }

    companion object {
        val INPUT_FORMAT: InputFormatConfigurationProperty =
            InputFormatConfigurationProperty.builder().deserializer(DeserializerProperty.builder().openXJsonSerDe(OpenXJsonSerDeProperty.builder().build()).build()).build()!!
        val OUTPUT_FORMAT: OutputFormatConfigurationProperty =
            OutputFormatConfigurationProperty.builder().serializer(SerializerProperty.builder().parquetSerDe(ParquetSerDeProperty.builder().build()).build()).build()!!
    }
}