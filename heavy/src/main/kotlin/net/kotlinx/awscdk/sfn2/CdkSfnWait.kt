package net.kotlinx.awscdk.sfn2

import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.Wait
import software.amazon.awscdk.services.stepfunctions.WaitProps
import software.amazon.awscdk.services.stepfunctions.WaitTime
import kotlin.time.Duration

class CdkSfnWait(
    override val sfn: CdkSfn,
    override val id: String,
    override val stateName: String,
) : CdkSfnChain {

    /** 하드코딩 시간 */
    var duration: Duration? = null

    /**
     * 언제까지 대기할지?
     * @see net.kotlinx.aws.AwsNaming.SCHEDULE_TIME
     *  */
    var timestampPath: String? = null

    /**
     * 몇초 대기할지?
     * @see net.kotlinx.aws.AwsNaming.WAIT_SECONDS
     * */
    var secondsPath: String? = null

    override fun convert(): State {
        val waitTime = when {
            duration != null -> WaitTime.duration(software.amazon.awscdk.Duration.millis(duration!!.inWholeMilliseconds))
            timestampPath != null -> WaitTime.timestampPath("$.${timestampPath}")
            secondsPath != null -> WaitTime.secondsPath("$.${secondsPath}")
            else -> throw IllegalArgumentException("option is null")
        }
        return Wait(sfn.stack, "${sfn.logicalName}-${id}", WaitProps.builder().time(waitTime).stateName(stateName).build())
    }
}