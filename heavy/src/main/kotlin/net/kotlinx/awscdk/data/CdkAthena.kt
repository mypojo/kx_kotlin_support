package net.kotlinx.awscdk.data

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.athena.CfnWorkGroup
import software.amazon.awscdk.services.athena.CfnWorkGroupProps
import software.amazon.awscdk.services.glue.CfnDatabase
import software.amazon.awscdk.services.glue.CfnDatabaseProps

/**
 * 따로 만들게 수정하자.. database 분리할일이 많음
 * */
@Deprecated("따로 만들어 쓰세요")
class CdkAthena : CdkInterface {

    @Kdsl
    constructor(block: CdkAthena.() -> Unit = {}) {
        apply(block)
    }

    /** DB 명.. 좋지 않음 */
    override val logicalName: String
        get() = deploymentType.name.lowercase().substring(0, 1)

    /** 결과 쿼리가 저장될 work 버킷 */
    lateinit var bucketName: String

    /** 쿼리 스켄 리미트 설정. 기본 10기가 */
    var bytesScannedCutoffPerQueryGb: Int = 10

    /** 결과1 */
    lateinit var database: CfnDatabase

    /** 결과2 */
    lateinit var workGroup: CfnWorkGroup

    /** 데이터베이스와 워크 그룹을 만들어준다 */
    fun create(stack: Stack) {
        val depName = deploymentType.name.lowercase()
        database = CfnDatabase(
            stack, "glue_db_${logicalName}-$depName", CfnDatabaseProps.builder()
                .catalogId(awsConfig.awsId) //계정 ID임
                .databaseInput(
                    CfnDatabase.DatabaseInputProperty.builder()
                        .name(logicalName)
                        .description("default db - $deploymentType")
                        .build()
                )
                .build()
        )
        TagUtil.tagDefault(database)

        val workgroupName = "workgroup-$depName"
        workGroup = CfnWorkGroup(
            stack, workgroupName, CfnWorkGroupProps.builder()
                .name(workgroupName)
                .description("${projectName} workGroup for $depName")
                .workGroupConfiguration(
                    CfnWorkGroup.WorkGroupConfigurationProperty.builder()
                        .bytesScannedCutoffPerQuery(GB_TO_BYTE * bytesScannedCutoffPerQueryGb)
                        .resultConfiguration(
                            CfnWorkGroup.ResultConfigurationProperty.builder()
                                .outputLocation("s3://${bucketName}/athena/outputLocation").build()
                        )
                        .build()
                )
                .build()
        )
        TagUtil.tagDefault(workGroup)
    }

    companion object {
        var GB_TO_BYTE: Long = 1073741824
    }
}