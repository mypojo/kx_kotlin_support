package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.athena.CfnWorkGroup
import software.amazon.awscdk.services.athena.CfnWorkGroupProps
import software.amazon.awscdk.services.glue.CfnDatabase
import software.amazon.awscdk.services.glue.CfnDatabaseProps

class CdkAthena(
    val project: CdkProject,
    block: CdkAthena.() -> Unit = {},
) {

    var deploymentType: DeploymentType = DeploymentType.dev

    /** 결과 쿼리가 저장될 work 버킷 */
    lateinit var bucketName: String

    init {
        block(this)
    }

    /** 데이터베이스와 워크 그룹을 만들어준다 */
    fun create(stack: Stack) {
        val dbName = deploymentType.name.substring(0, 1)
        val database = CfnDatabase(
            stack, "glue_db_${dbName}-${deploymentType}", CfnDatabaseProps.builder()
                .catalogId(project.awsId) //계정 ID임
                .databaseInput(
                    CfnDatabase.DatabaseInputProperty.builder()
                        .name(dbName)
                        .description("default db - $deploymentType")
                        .build()
                )
                .build()
        )
        TagUtil.tag(database, deploymentType)

        val workgroupName = "workgroup-${deploymentType}"
        val workGroup = CfnWorkGroup(
            stack, workgroupName, CfnWorkGroupProps.builder()
                .name(workgroupName)
                .description("${project.projectName} workGroup for $deploymentType")
                .workGroupConfiguration(
                    CfnWorkGroup.WorkGroupConfigurationProperty.builder()
                        .bytesScannedCutoffPerQuery(GB_TO_BYTE * 1)
                        .resultConfiguration(
                            CfnWorkGroup.ResultConfigurationProperty.builder()
                                .outputLocation("s3://${bucketName}/athena/outputLocation").build()
                        )

                        .build()
                )
                .build()
        )
        TagUtil.tag(workGroup, deploymentType)
    }

    companion object {
        var GB_TO_BYTE: Long = 1073741824
    }
}