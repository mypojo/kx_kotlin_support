package net.kotlinx.awscdk.data

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.athena.CfnWorkGroup
import software.amazon.awscdk.services.athena.CfnWorkGroupProps

/**
 * 아테나 워크그룹
 * */
class CdkAthenaWorkGroup : CdkInterface {

    @Kdsl
    constructor(block: CdkAthenaWorkGroup.() -> Unit = {}) {
        apply(block)
    }

    /** DB 명.. 좋지 않음 */
    override val logicalName: String
        get() = "workgroup-${suff}"

    /** 결과 쿼리가 저장될 work 버킷 */
    lateinit var bucketName: String

    /** 쿼리 스켄 리미트 설정. 기본 10기가 */
    var bytesScannedCutoffPerQueryGb: Int = 10

    /** 결과2 */
    lateinit var workGroup: CfnWorkGroup

    /** 데이터베이스와 워크 그룹을 만들어준다 */
    fun create(stack: Stack) {
        workGroup = CfnWorkGroup(
            stack, logicalName, CfnWorkGroupProps.builder()
                .name(logicalName)
                .description("${projectName} workGroup for $suff")
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