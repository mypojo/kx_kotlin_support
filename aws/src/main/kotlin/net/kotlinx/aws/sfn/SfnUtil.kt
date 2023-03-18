package net.kotlinx.aws.sfn

import net.kotlinx.aws1.AwsConfig

object SfnUtil {

    /** 실행 ARN  */
    fun stateMachineArn(awsId: String, stateMachineName: String, uuid: String, region: String = AwsConfig.SEOUL): String {
        return "arn:aws:states:${region}:${awsId}:execution:${stateMachineName}:${uuid}"
    }

    /** 머신 ARN  */
    fun buildMachineArn(awsId: String, stateMachineName: String, region: String = AwsConfig.SEOUL): String {
        return "arn:aws:states:${region}:${awsId}:stateMachine:${stateMachineName}"
    }
}