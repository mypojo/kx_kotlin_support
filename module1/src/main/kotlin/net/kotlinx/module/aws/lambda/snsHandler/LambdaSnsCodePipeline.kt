package net.kotlinx.module.aws.lambda.snsHandler

import com.google.common.eventbus.EventBus
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.core.gson.GsonData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 코드파이프라인 결과
 * */
class LambdaSnsCodePipeline(
    val title: String = "CodePipeline 빌드결과"
) : (GsonData) -> String?, KoinComponent {

    private val eventBus: EventBus by inject()

    override fun invoke(sns: GsonData): String? {

        if (sns["detailType"].str != "CodePipeline Pipeline Execution State Change") return null

        val detail = sns["detail"]
        val msg = "pipeline [${detail["pipeline"]}] => ${detail["state"]}"

        eventBus.post(LambdaSnsEvent(title, msg))
        return LambdaUtil.OK
    }
}

private const val sample = """
{
  "account": "653734769926",
  "detailType": "CodePipeline Action Execution State Change",
  "region": "ap-northeast-2",
  "source": "aws.codepipeline",
  "time": "2022-07-21T05:26:58Z",
  "notificationRuleArn": "arn:aws:codestar-notifications:ap-northeast-2:653734769926:notificationrule/c36fb6d6784c2d31705a84702f249513ab5bbec1",
  "detail": {
    "pipeline": "sin-dev",
    "execution-id": "1d247af2-7eb3-4569-aeda-41416e2a81c5",
    "stage": "build",
    "execution-result": {
      "external-execution-url": "https://console.aws.amazon.com/codebuild/home?region=ap-northeast-2#/builds/sin-dev:86a1cd00-9509-4929-ac8b-7d3a8c10c2bc/view/new",
      "external-execution-summary": "Build terminated with state: FAILED",
      "external-execution-id": "sin-dev:86a1cd00-9509-4929-ac8b-7d3a8c10c2bc",
      "error-code": "JobFailed"
    },
    "action": "Build",
    "state": "FAILED",
    "region": "ap-northeast-2",
    "type": {
      "owner": "AWS",
      "provider": "CodeBuild",
      "category": "Build",
      "version": "1"
    },
    "version": 11.0
  },
  "resources": [
    "arn:aws:codepipeline:ap-northeast-2:653734769926:sin-dev"
  ],
  "additionalAttributes": {
    "additionalInformation": "Build terminated with state: FAILED"
  }
}    
    """
