package net.kotlinx.awscdk.component.sfnv2

import net.kotlinx.aws.AwsNaming
import software.amazon.awscdk.services.stepfunctions.CustomState
import software.amazon.awscdk.services.stepfunctions.CustomStateProps
import software.amazon.awscdk.services.stepfunctions.State


/**
 * SFN MAP 작업 : S3 list -> lambda
 * 병렬 작업하는애들 개별 상태관리 하지 않고, 인라인으로 관리함 (저렴)
 * */
class CdkSfnMapInline(
    override val cdkSfn: CdkSfn,
    override val name: String,
) : CdkSfnChain {

    override var suffix: String = ""

//    /** 실행할 람다 이름 */
//    var lambdaName: String = "${cdkSfn.lambda.functionName}:${LambdaUtil.SERVICE_ON}"

    /** 스탭 내부의 state 이름 */
    var stepName: String = "${name}Inline"

    var itemPath: String = "$.StepStart.datas"

    var resultPath: String = "$.${AwsNaming.OPTION}.${name}"

    /** DISTRIBUTED 모드는 람다 리밋까지 지원 */
    var maxConcurrency: Int = 40

    /** 컴포넌트가 없어서 수동으로 지정해야 한다.. 없으면 종료 */
    var next: String? = null

    //==================================================== 오류 3종 ======================================================

    var retryIntervalSeconds: Int = 10 // 적게 주어야 더 빠르게 작동할듯
    var backoffRate: Double = 1.2 //오류시 리트라이 증분. IP 블록 우회하는 크롤링이라면 동시에 실행되어야 람다가 다르게 실생되서 분산된다.
    var maxAttempts: Int = 3

    /** 별도 설정이 없어서 노가다 했음.. 차라리 이게 더 나은듯.. */
    override fun convert(): State {
        val stateMap = mapOf(
            "Type" to "Map",
            when (next) {
                null -> "End" to true
                else -> "Next" to next
            },
            "MaxConcurrency" to maxConcurrency,
            "ItemsPath" to itemPath,
            "ResultPath" to resultPath, // 별도의 ResultSelector 는 필요없다. 최종작업의 결과만 ResultSelector 하면 그게 array로 입력됨
            "ItemProcessor" to mapOf(
                "ProcessorConfig" to mapOf(
                    "Mode" to "INLINE",
                ),
                "StartAt" to stepName, //lambdaName 내부 json key 하고 일치해야함. 내부 state의 이름.
                "States" to mapOf(
                    stepName to mapOf(
                        "Type" to "Task",
                        "Resource" to "arn:aws:states:::lambda:invoke",
                        "OutputPath" to "$.Payload",
                        "Parameters" to mapOf(
                            "FunctionName" to cdkSfn.lambda.functionArn,
                            "Payload.$" to "$"
                        ),
                        "Retry" to listOf(
                            mapOf(
                                "ErrorEquals" to listOf(
                                    "Lambda.ServiceException",
                                    "Lambda.AWSLambdaException",
                                    "Lambda.SdkClientException",
                                    "Lambda.TooManyRequestsException",
                                    "States.TaskFailed", //태스크가 오류난거도 리트라이 해준다.  (특정 예외 캐치 기능은 아직 없는듯.. retryException만 리트라이 하고싶다)
                                ),
                                "IntervalSeconds" to retryIntervalSeconds,
                                "BackoffRate" to backoffRate, //기본값
                                "MaxAttempts" to maxAttempts, //기본값
                            )
                        ),
                        "End" to true,
                    )
                )
            ),
        )
        return CustomState(cdkSfn.stack, "${name}${suffix}", CustomStateProps.builder().stateJson(stateMap).build())
    }

}