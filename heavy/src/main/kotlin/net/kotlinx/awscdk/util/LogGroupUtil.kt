package net.kotlinx.awscdk.util

import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.LogGroupProps
import software.amazon.awscdk.services.logs.RetentionDays

object LogGroupUtil {

    /** 테스트 CDK 생성시 DESTROY로 해야 오류가 안남 */
    fun create(stack: Stack, serviceName: String, deploymentType: DeploymentType, retentionDays: RetentionDays = RetentionDays.FIVE_YEARS): LogGroup {
        val name = "/aws/${serviceName}/job-${deploymentType.name.lowercase()}"
        return LogGroup(
            stack, name, LogGroupProps.builder()
                .logGroupName(name)
                .retention(retentionDays)
                .removalPolicy(RemovalPolicy.DESTROY) //편의상 DESTROY
                .build()
        )
    }

}