package net.kotlinx.aws.sfn

import net.kotlinx.aws.AwsConfig

/**
 * 네이밍 여기로 통일
 * */
class SfnConfig(private val awsConfig: AwsConfig) {


    /** 실행 ARN  */
    fun executionArn(stateMachineName: String, uuid: String): String {
        return "arn:aws:states:${awsConfig.region}:${awsConfig.awsId}:execution:${stateMachineName}:${uuid}"
    }

    /** 머신 ARN  */
    fun stateMachineArn(stateMachineName: String): String {
        return "arn:aws:states:${awsConfig.region}:${awsConfig.awsId}:stateMachine:${stateMachineName}"
    }

    /** 콘솔 링크 */
    fun consoleLink(stateMachineName: String, uuid: String): String {
        val executionArn = executionArn(stateMachineName, uuid)
        return "https://$awsConfig.region.console.aws.amazon.com/states/home?region=$${awsConfig.region}#/v2/executions/details/$executionArn"
    }

    companion object {

        private const val CONSOLE = "https://ap-northeast-2.console.aws.amazon.com/states/home?region=ap-northeast-2#/v2/executions/details/"

        //==================================================== CDK 예약어 ======================================================

        /** 일반적인 잡 옵션 */
        const val SFN_ID: String = "sfnId"
    }

}