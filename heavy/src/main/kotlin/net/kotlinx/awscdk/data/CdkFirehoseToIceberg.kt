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
 * 입력/수정/삭제가 되는 아이스버그 공통 스트림 -> 라우터 설정해서 각 테이블 입력/수정/삭제 가능
 * 레이크포메이션 사용하는경우 IAM 외 추라고 레이크포메이션 콘솔에서 role에 태그 권한 지정 해줘야함
 * IcebergRouter 참고
 *
 * 생성후 최적화 설정도 잡아줘야함
 * @see aws.sdk.kotlin.services.glue.GlueClient createTableOptimizerByDefault
 *
 * 경고!! S3 버킷 테이블로의 직접 입력은 아직 지원하지 않는다 (2025-03)
 *  */
class CdkFirehoseToIceberg : CdkInterface {

    @Kdsl
    constructor(block: CdkFirehoseToIceberg.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "${streamName}-${suff}"

    /** 스크림 네임 */
    lateinit var streamName: String

    /** KDF 전송 역할 */
    lateinit var role: IRole

    /** 기본 S3저장 버킷 */
    lateinit var bucket: IBucket

    /**
     * 라우터 정보 매핑 설정
     * 순서대로 DB, 테이블, 오퍼레이터
     */
    data class RouterQuery(
        val db: String = ".route.db",
        val table: String = ".route.table",
        val op: String = ".route.op",
    )

    var routerQuery: RouterQuery = RouterQuery()


    /**
     * 라우터를 쿼리로.  주의!! json처럼 생겼지만 아님!
     * ex) {basic_date:.basic_date,name:.name}
     * */
    private val routerQueryText: String
        get() = "{destinationDatabaseName:${routerQuery.db},destinationTableName:${routerQuery.table},operation:${routerQuery.op}}"

    /** 테이블 구성 정보 */
    data class TableConfig(
        /** 데이터베이스 이름 */
        val databaseName: String,
        /** 테이블 이름 */
        val tableName: String,
        /** 유니크 키 목록 */
        val uniqueKeys: List<String>,
    )

    /**
     * 이 스트림에서 사용할 모든 테이블 나열
     * 주의!!! 이거는 한번 설정하면 변경 못함.
     * 수정시 삭제 후 재생성 해야함
     */
    lateinit var tableConfigs: List<TableConfig>

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
                /** 디폴트로 기본 암호화 설정 */
                .deliveryStreamEncryptionConfigurationInput(DeliveryStreamEncryptionConfigurationInputProperty.builder().keyType("AWS_OWNED_CMK").build())
                /** s3DestinationConfiguration 가 아닌 확장을 사용. 순서는 AWS 콘솔의 JSON 기준 */
                .icebergDestinationConfiguration(
                    IcebergDestinationConfigurationProperty.builder()
                        .roleArn(role.roleArn)
                        .catalogConfiguration(CatalogConfigurationProperty.builder().catalogArn("arn:aws:glue:${awsConfig.region}:${awsConfig.awsId}:catalog").build())
                        /** 버퍼 용량 128은 거의 고정 권장 */
                        .bufferingHints(BufferingHintsProperty.builder().sizeInMBs(128).intervalInSeconds(intervalInSeconds).build())
                        /** 기본 300초. */
                        .retryOptions(RetryOptionsProperty.builder().durationInSeconds(300).build())
                        /** 클라우드와치 필요없음.. 잘 안되는경우 에러 메세지 잘 나옴 */
                        .cloudWatchLoggingOptions(CloudWatchLoggingOptionsProperty.builder().enabled(false).build())
                        /** 여기서 등록될 수 있는 모든 테이블 명시 */
                        .destinationTableConfigurationList(tableConfigs.map {
                            DestinationTableConfigurationProperty.builder()
                                .destinationDatabaseName(it.databaseName)
                                .destinationTableName(it.tableName)
                                .uniqueKeys(it.uniqueKeys)
                                .s3ErrorOutputPrefix("error")
                                .build()
                        })
                        /** 실패시에만 로그파일 생성 */
                        .s3BackupMode("FailedDataOnly")

                        .processingConfiguration(
                            ProcessingConfigurationProperty.builder().enabled(true)
                                .processors(
                                    listOf(
                                        ProcessorProperty.builder().type("MetadataExtraction")
                                            .parameters(
                                                listOf(
                                                    ProcessorParameterProperty.builder().parameterName("MetadataExtractionQuery").parameterValue(routerQueryText).build(),
                                                    ProcessorParameterProperty.builder().parameterName("JsonParsingEngine").parameterValue("JQ-1.6").build(),
                                                )
                                            )
                                            .build()
                                    )
                                )
                                .build()
                        )

                        .s3Configuration(
                            S3DestinationConfigurationProperty.builder()
                                .bucketArn(bucket.bucketArn)
                                .roleArn(role.roleArn)
                                .errorOutputPrefix("error/firehose/${logicalName}/")
                                .build()
                        )
                        .cloudWatchLoggingOptions(CloudWatchLoggingOptionsProperty.builder().enabled(false).build())
                        .build()
                )
                .build()
        )
        TagUtil.tagDefault(deliveryStream)
    }

}