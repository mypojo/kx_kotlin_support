package net.kotlinx.aws_cdk.component.sfn

import net.kotlinx.aws.sfn.SfnUtil
import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.Wait
import software.amazon.awscdk.services.stepfunctions.WaitProps
import software.amazon.awscdk.services.stepfunctions.WaitTime

data class SfnWait(
    /** 필드 이름 */
    override val name: String,
    override var suffix: String = ""
) : SfnChain {
    var timestampPath: String = "$.${SfnUtil.jobScheduleTime}"

    override fun convert(cdk: CdkSfn): State {
        return Wait(
            cdk.stack, "${name}${suffix}", WaitProps.builder()
                .time(WaitTime.timestampPath(timestampPath))
                .build()
        )
    }
}