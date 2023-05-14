package net.kotlinx.aws_cdk.component.sfn

import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.Wait
import software.amazon.awscdk.services.stepfunctions.WaitProps
import software.amazon.awscdk.services.stepfunctions.WaitTime

class SfnWait(
    /** 필드 이름 */
    override val name: String,
    override var suffix: String = ""
) : SfnChain {

    /**
     * 언제까지 대기할지?
     * @see net.kotlinx.aws1.AwsNaming.scheduleTime
     *  */
    var timestampPath: String? = null

    /**
     * 몇초 대기할지?
     * @see net.kotlinx.aws1.AwsNaming.waitSeconds
     * */
    var secondsPath: String? = null

    override fun convert(cdk: CdkSfn): State {
        val waitTime = when {
            timestampPath != null -> WaitTime.timestampPath("$.${timestampPath}")
            secondsPath != null -> WaitTime.secondsPath("$.${secondsPath}")
            else -> throw IllegalArgumentException("option is null")
        }
        return Wait(cdk.stack, "${name}${suffix}", WaitProps.builder().time(waitTime).build())
    }
}