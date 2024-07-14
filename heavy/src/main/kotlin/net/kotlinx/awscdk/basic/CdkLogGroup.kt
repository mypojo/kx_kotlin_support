package net.kotlinx.awscdk.basic

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.LogGroupProps
import software.amazon.awscdk.services.logs.RetentionDays

class CdkLogGroup : CdkInterface {

    @Kdsl
    constructor(block: CdkLogGroup.() -> Unit = {}) {
        apply(block)
    }

    /** VPC 이름 */
    override val logicalName: String
        get() = "/aws/${serviceName}-${deploymentType.name.lowercase()}"

    /**
     * 서비스명.  이걸로 로그 패스가 결정됨
     * ex) ecs/job
     *  */
    lateinit var serviceName: String

    /** 로그 보관 주기 */
    var retentionDays: RetentionDays = RetentionDays.FIVE_YEARS

    /** 결과 */
    lateinit var logGroup: LogGroup

    /** 테스트 CDK 생성시 DESTROY로 해야 오류가 안남 */
    fun create(stack: Stack) {
        logGroup = LogGroup(
            stack, logicalName, LogGroupProps.builder()
                .logGroupName(logicalName)
                .retention(retentionDays)
                .removalPolicy(RemovalPolicy.DESTROY) //편의상 DESTROY
                .build()
        )
    }

}