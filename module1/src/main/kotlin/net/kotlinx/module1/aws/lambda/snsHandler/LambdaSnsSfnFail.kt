package net.kotlinx.module1.aws.lambda.snsHandler

import com.google.common.eventbus.EventBus
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.core.gson.GsonData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * SFN 실패 알람
 * 프로젝트별로 오버라이드 해주세요
 * */
class LambdaSnsSfnFail(
    val title: String = "SFN job fail",
) : (GsonData) -> String?, KoinComponent {

    private val eventBus: EventBus by inject()

    override fun invoke(sns: GsonData): String? {

        if (sns["detail-type"].str != "Step Functions Execution Status Change") return null

        val detail = sns["detail"]
        val jobName = detail["stateMachineArn"].str!!.substringAfterLast(":")

        val title = "[$jobName] SFN job fail"
        val msg = detail["cause"].str!!

        eventBus.post(LambdaSnsEvent(title, msg))
        return LambdaUtil.Ok

    }
}

private val sample = """
{
  "version": "0",
  "id": "d43b45a8-ea4f-c10a-6cc3-89bce22abc08",
  "detail-type": "Step Functions Execution Status Change",
  "source": "aws.states",
  "account": "463327615611",
  "time": "2023-04-25T00:47:17Z",
  "region": "ap-northeast-2",
  "resources": [
    "arn:aws:states:ap-northeast-2:463327615611:execution:sin-rpt_demo-dev:dd0c252d-4f2a-b727-5e9a-43773b55a06a"
  ],
  "detail": {
    "executionArn": "arn:aws:states:ap-northeast-2:463327615611:execution:sin-rpt_demo-dev:dd0c252d-4f2a-b727-5e9a-43773b55a06a",
    "stateMachineArn": "arn:aws:states:ap-northeast-2:463327615611:stateMachine:sin-rpt_demo-dev",
    "name": "dd0c252d-4f2a-b727-5e9a-43773b55a06a",
    "status": "FAILED",
    "startDate": 1682383637227,
    "stopDate": 1682383637293,
    "input": "{\n    \\",
    "Comment\\\\\\": ": \\\"Insert your JSON here\\\"\\n}\",\"output\":null,\"inputDetails\":{\"included\":true},\"outputDetails\":null,\"error\":\"States.Runtime\",\"cause\":\"An error occurred while executing the state 'Sample01Job' (entered at the event id #4). The JSONPath '${'$'}.jobOption' specified for the field 'jobOption.${'$'}' could not be found in the input '{\\n    \\\"Comment\\\": \\\"Insert your JSON here\\\"\\n}'\"}}"
  }
}    
""".trimIndent()