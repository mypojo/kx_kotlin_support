package net.kotlinx.aws.sfn

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.sts.StsUtil
import net.kotlinx.koin.Koins.koin

/**
 * 네이밍 여기로 통일
 * */
object SfnUtil {

    /** ?? */
    private const val CONSOLE = "https://ap-northeast-2.console.aws.amazon.com/states/home?region=ap-northeast-2#/v2/executions/details/"

    //==================================================== CDK 예약어 ======================================================

    /** 일반적인 잡 옵션 */
    const val SFN_ID: String = "sfnId"

    /** 실행 ARN  */
    fun executionArn(stateMachineName: String, uuid: String, region: String = koin<AwsConfig>().region): String {
        return "arn:aws:states:${region}:${StsUtil.ACCOUNT_ID}:execution:${stateMachineName}:${uuid}"
    }

    /** 머신 ARN  */
    fun stateMachineArn(stateMachineName: String, region: String = koin<AwsConfig>().region): String {
        return "arn:aws:states:${region}:${StsUtil.ACCOUNT_ID}:stateMachine:${stateMachineName}"
    }

    /** 콘솔 링크 */
    fun consoleLink(stateMachineName: String, uuid: String, region: String = koin<AwsConfig>().region): String {
        val executionArn = executionArn(stateMachineName, uuid, region)
        return "https://$region.console.aws.amazon.com/states/home?region=$region#/v2/executions/details/$executionArn"
    }

}