package net.kotlinx.aws_cdk.component.sfnv2

import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.Wait
import software.amazon.awscdk.services.stepfunctions.WaitProps
import software.amazon.awscdk.services.stepfunctions.WaitTime
import kotlin.time.Duration

class CdkSfnWait(
    override val cdkSfn: CdkSfn,
    override val name: String,
) : CdkSfnChain {

    override var suffix: String = ""

    /** 하드코딩 시간 */
    var duration: Duration? = null

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

    override fun convert(): State {
        val waitTime = when {
            duration != null -> WaitTime.duration(software.amazon.awscdk.Duration.millis(duration!!.inWholeMilliseconds))
            timestampPath != null -> WaitTime.timestampPath("$.${timestampPath}")
            secondsPath != null -> WaitTime.secondsPath("$.${secondsPath}")
            else -> throw IllegalArgumentException("option is null")
        }
        return Wait(cdkSfn.stack, "${name}${suffix}", WaitProps.builder().time(waitTime).build())
    }
}