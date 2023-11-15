package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.athena.CfnWorkGroup
import software.amazon.awscdk.services.athena.CfnWorkGroupProps
import software.amazon.awscdk.services.glue.CfnDatabase
import software.amazon.awscdk.services.glue.CfnDatabaseProps

class CdkAthena : CdkInterface {

    @Kdsl
    constructor(block: CdkAthena.() -> Unit = {}) {
        apply(block)
    }

    /** DB 명 */
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
                .catalogId(project.awsId) //계정 ID임
                .databaseInput(
                    CfnDatabase.DatabaseInputProperty.builder()
                        .name(logicalName)
                        .description("default db - $deploymentType")
                        .build()
                )
                .build()
        )
        TagUtil.tag(database,deploymentType)

        val workgroupName = "workgroup-$depName"
        workGroup = CfnWorkGroup(
            stack, workgroupName, CfnWorkGroupProps.builder()
                .name(workgroupName)
                .description("${project.projectName} workGroup for $depName")
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
        TagUtil.tag(workGroup,deploymentType)
    }

    companion object {
        var GB_TO_BYTE: Long = 1073741824
    }
}