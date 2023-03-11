package net.kotlinx.aws_cdk.module

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream.*
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStreamProps
import software.amazon.awscdk.services.s3.IBucket


/**
 * 직접 KDF를 호출하는 스트림 생성
 *  */
class KdfModule(
    val project: CdkProject,
    val stack: Stack,
    val bucket: IBucket,
    val role: IRole,
    val deploymentType: DeploymentType,
    val tablePrefix: String = "event1_",
) {

    /** KDF 를 다이렉트로 파케이 변환 */
    fun parquet(tableName: String, prefix: String, parameterValue: String) {
        val deliveryStreamName = "${tableName}-${deploymentType}"
        val intervalInSeconds = if (deploymentType == DeploymentType.prod) 60 * 10 else 60 //실서버는 10분에 한번 로깅. 60이 최소. 최초 개발시 빠른 반응을 위해서 60으로 하자.

        val firehoseDeliveryStream = CfnDeliveryStream(
            stack, "kdf-$deliveryStreamName", CfnDeliveryStreamProps.builder()
                .deliveryStreamName(deliveryStreamName)
                .deliveryStreamType("DirectPut")
                .extendedS3DestinationConfiguration(  //s3DestinationConfiguration 가 아닌 확장을 사용
                    ExtendedS3DestinationConfigurationProperty.builder()
                        .bucketArn(bucket.bucketArn)
                        .bufferingHints(BufferingHintsProperty.builder().sizeInMBs(10).intervalInSeconds(intervalInSeconds).build())
                        .compressionFormat("UNCOMPRESSED")  //must be set to UNCOMPRESSED when data format conversion is enabled -> 실제는 스내피로 적용될듯
                        .encryptionConfiguration(EncryptionConfigurationProperty.builder().noEncryptionConfig("NoEncryption").build()) //암호화 안함
                        .roleArn(role.roleArn)
                        //S3 경로에는 대소문자가 구분됨. 하지만 헷갈리니 언더스코어로 하자.
                        .prefix("firehose/${tableName}/basicDate=!{timestamp:yyyyMMdd}/hh=!{timestamp:HH}/")
                        .errorOutputPrefix("firehose/${tableName}_!{firehose:error-output-type}/basicDate=!{timestamp:yyyyMMdd}/hh=!{timestamp:HH}/")
                        .build()
                )
                .build()
        )

//        extendedS3DestinationConfiguration: {
//            bucketArn: bucket.bucketArn,
//            bufferingHints: {sizeInMBs: KDF_MAX_MB, intervalInSeconds: intervalInSeconds}, // 최초 개발시 빠른 반응 원함 -> 60초
//            compressionFormat: "UNCOMPRESSED",  //must be set to UNCOMPRESSED when data format conversion is enabled -> 실제는 스내피로 적용될듯
//            dataFormatConversionConfiguration: {
//            enabled: true,
//            inputFormatConfiguration: {deserializer: {openXJsonSerDe: {}}},
//            outputFormatConfiguration: {serializer: {parquetSerDe: {}}},
//            schemaConfiguration: {
//                databaseName: databaseName,
//                region: THE.region,
//                roleArn: role.roleArn,
//                tableName: tableName,
//                versionId: 'LATEST'
//        }
//        },
//            dynamicPartitioningConfiguration: {
//            enabled: true,
//            retryOptions: {durationInSeconds: 300}
//        },
//            encryptionConfiguration: {noEncryptionConfig: "NoEncryption" /*암호화 안함*/},
//            roleArn: role.roleArn,
//            //S3 경로에는 대소문자가 구분됨. 하지만 헷갈리니 언더스코어로 하자.
//            prefix: prefix,
//            errorOutputPrefix: `data/${tableName}/errors/`,
//            processingConfiguration: {
//            enabled: true,
//            processors: [
//            {type: "AppendDelimiterToRecord"},
//            {
//                type: "MetadataExtraction", parameters: [
//                {parameterName: "MetadataExtractionQuery", parameterValue: parameterValue},
//                {parameterName: "JsonParsingEngine", parameterValue: "JQ-1.6"},
//                ]
//            },
//            ]
//        }
//        }


    }
}