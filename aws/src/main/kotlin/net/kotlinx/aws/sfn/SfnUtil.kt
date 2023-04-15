package net.kotlinx.aws.sfn

import net.kotlinx.aws1.AwsConfig

object SfnUtil {


    //==================================================== CDK 예약어 ======================================================

    /** 일반적인 잡 옵션 */
    const val sfnId: String = "sfnId"

    /** 일반적인 잡 옵션 */
    const val jobOption: String = "jobOption"

    /** 잡 스케쥴링 옵션 */
    const val jobScheduleTime: String = "jobScheduleTime"

    /** 실행 ARN  */
    fun stateMachineArn(awsId: String, stateMachineName: String, uuid: String, region: String = AwsConfig.SEOUL): String {
        return "arn:aws:states:${region}:${awsId}:execution:${stateMachineName}:${uuid}"
    }

    /** 머신 ARN  */
    fun buildMachineArn(awsId: String, stateMachineName: String, region: String = AwsConfig.SEOUL): String {
        return "arn:aws:states:${region}:${awsId}:stateMachine:${stateMachineName}"
    }
}