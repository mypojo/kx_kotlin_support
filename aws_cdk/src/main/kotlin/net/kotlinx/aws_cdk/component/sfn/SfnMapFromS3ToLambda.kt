package net.kotlinx.aws_cdk.component.sfn

import software.amazon.awscdk.services.stepfunctions.CustomState
import software.amazon.awscdk.services.stepfunctions.CustomStateProps
import software.amazon.awscdk.services.stepfunctions.State


/**
 * SFN MAP 작업 : S3 list -> lambda
 * 다수의 데이터를 대량처리할때 사용된다
 * */
class SfnMapFromS3ToLambda(
    override val name: String,
    override var suffix: String = ""
) : SfnChain {

    /** 실행할 람다 이름 */
    lateinit var lambdaName: String

    /** DISTRIBUTED 모드라서 거의 무한대로 지원함 */
    var maxConcurrency: Int = 0

    /** 버킷명 키 */
    var bucket: String = SfnMapFromS3ToLambda.bucket

    /** S3 경로  키 */
    var key: String = SfnMapFromS3ToLambda.key

    //==================================================== 오류 3종 ======================================================

    /** 네이버 크롤링=4초 */
    var retryIntervalSeconds: Int = 4 // 적게 주어야 더 빠르게 작동할듯
    var backoffRate: Double = 1.00 //오류시 리트라이 증분. IP 블록 우회하는 크롤링이라면 동시에 실행되어야 람다가 다르게 실생되서 분산된다.
    var maxAttempts: Int = 100

    /** 별도 설정이 없어서 노가다 했음.. 차라리 이게 더 나은듯.. */
    override fun convert(cdkSfn: CdkSfn): State {
        return CustomState(
            cdkSfn.stack, "${name}${suffix}", CustomStateProps.builder()
                .stateJson(
                    mapOf(
                        "Type" to "Map",
                        "End" to true,
                        "Label" to "invokeEach", //이름 대충 붙여준다.
                        "MaxConcurrency" to maxConcurrency,
                        "ItemProcessor" to mapOf(
                            "ProcessorConfig" to mapOf(
                                "Mode" to "DISTRIBUTED",
                                "ExecutionType" to "STANDARD",
                            ),
                            "StartAt" to lambdaName,
                            "States" to mapOf(
                                lambdaName to mapOf(
                                    "Type" to "Task",
                                    "Resource" to "arn:aws:states:::lambda:invoke",
                                    "OutputPath" to "$.Payload",
                                    "Parameters" to mapOf(
                                        "FunctionName" to "arn:aws:lambda:${cdkSfn.project.region}:${cdkSfn.project.awsId}:function:${lambdaName}",
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
                        "ItemReader" to mapOf(
                            "Resource" to "arn:aws:states:::s3:listObjectsV2",
                            "Parameters" to mapOf(
                                "Bucket.$" to "$.${bucket}",
                                "Prefix.$" to "$.${key}",
                            ),
                        )
                    )
                )
                .build()
        )
    }

    companion object {
        @Deprecated("xx")
        const val bucket = "bucket"

        @Deprecated("xx")
        const val key = "key"
    }

}