package net.kotlinx.aws_cdk.module

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream.*
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStreamProps
import software.amazon.awscdk.services.s3.IBucket


/**
 * 직접 KDF를 호출하면 테이블에 데이터를 쌓아주는 KDF 스트림 생성
 *  */
class KdfModule(
    val project: CdkProject,
    val stack: Stack,
    val bucket: IBucket,
    val role: IRole,
    val deploymentType: DeploymentType,
    val databaseName: String = project.projectName.substring(1),
) {

    /**
     * KDF 를 다이렉트로 파케이 변환
     * @param prefix ex) data/${tableName}/basic_date=!{partitionKeyFromQuery:basic_date}/name=!{partitionKeyFromQuery:name}/
     * @param parameterValue ex) {basic_date:.basic_date,name:.name}
     *  */
    fun parquet(tableName: String, prefix: String, parameterValue: String) {
        val deliveryStreamName = "${tableName}-${deploymentType}"
        val intervalInSeconds = if (deploymentType == DeploymentType.prod) 60 * 10 else 60 //실서버는 10분에 한번 로깅. 60이 최소. 최초 개발시 빠른 반응을 위해서 60으로 하자.

        CfnDeliveryStream(
            stack, "kdf-$deliveryStreamName", CfnDeliveryStreamProps.builder()
                .deliveryStreamName(deliveryStreamName)
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
                            DynamicPartitioningConfigurationProperty.builder().enabled(true).retryOptions(RetryOptionsProperty.builder().durationInSeconds(300).build()).build() //확인하지 못한 옵션
                        )
                        .processingConfiguration(
                            ProcessingConfigurationProperty.builder().enabled(true)
                                .processors(
                                    listOf(
                                        ProcessorProperty.builder().type("AppendDelimiterToRecord")
                                            .parameters(
                                                listOf(
                                                    ProcessorParameterProperty.builder().parameterName("MetadataExtractionQuery").parameterValue(parameterValue).build(),
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

    }

    companion object {
        val INPUT_FORMAT: InputFormatConfigurationProperty =
            InputFormatConfigurationProperty.builder().deserializer(DeserializerProperty.builder().openXJsonSerDe(OpenXJsonSerDeProperty.builder().build()).build()).build()!!
        val OUTPUT_FORMAT: OutputFormatConfigurationProperty =
            OutputFormatConfigurationProperty.builder().serializer(SerializerProperty.builder().parquetSerDe(ParquetSerDeProperty.builder().build()).build()).build()!!
    }
}