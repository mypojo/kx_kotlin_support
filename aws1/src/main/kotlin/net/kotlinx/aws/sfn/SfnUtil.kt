package net.kotlinx.aws.sfn

import net.kotlinx.aws.AwsConfig

object SfnUtil {

    private const val CONSOLE = "https://ap-northeast-2.console.aws.amazon.com/states/home?region=ap-northeast-2#/v2/executions/details/"

    //==================================================== CDK 예약어 ======================================================

    /** 일반적인 잡 옵션 */
    const val SFN_ID: String = "sfnId"

    /** 실행 ARN  */
    fun executionArn(awsId: String, stateMachineName: String, uuid: String, region: String = AwsConfig.SEOUL): String {
        return "arn:aws:states:${region}:${awsId}:execution:${stateMachineName}:${uuid}"
    }

    /** 머신 ARN  */
    fun stateMachineArn(awsId: String, stateMachineName: String, region: String = AwsConfig.SEOUL): String {
        return "arn:aws:states:${region}:${awsId}:stateMachine:${stateMachineName}"
    }

    /** 콘솔 링크 */
    fun consoleLink(awsId: String, stateMachineName: String, uuid: String,region: String = AwsConfig.SEOUL):String{
        val executionArn = executionArn(awsId, stateMachineName, uuid, region)
        return "https://$region.console.aws.amazon.com/states/home?region=$region#/v2/executions/details/$executionArn"
    }

}