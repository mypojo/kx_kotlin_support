package net.kotlinx.aws_cdk.component

import com.lectra.koson.obj
import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws.sfn.SfnUtil
import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core1.DeploymentType
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.stepfunctions.*
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJob
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJobProps
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvokeProps

sealed interface SfnChain {
    /** 잡 이름 */
    val name: String

    /** 중복으로 ID 충돌날경우 서픽스 */
    var suffix: String
}

data class SfnLambda(
    override val name: String,
    override var suffix: String = ""
) : SfnChain

data class SfnBatch(
    override val name: String,
    override var suffix: String = ""
) : SfnChain

data class SfnWait(
    /** 필드 이름 */
    override val name: String = SfnUtil.jobScheduleTime,
    override var suffix: String = ""
) : SfnChain

/**
 * ID 중복 최소한만 고려 (화면에 안예쁘게 나옴)
 * ex)  {"jobOption":{"sfnId":"9a25f502-588c-42e6-8be5-00955f1a60ac","basicDate":"20230414"},"jobOptionText":"{\"sfnId\":\"9a25f502-588c-42e6-8be5-00955f1a60ac\",\"basicDate\":\"20230414\"}"}
 * */
class CdkSfn(
    val project: CdkProject,
    val deploymentType: DeploymentType,
    val stack: Stack,
    val name: String,
    val timeout: Duration = Duration.hours(8), //새벽 기준으로 출근 할때까지
) : CdkInterface {

    override val logicalName: String = "${project.projectName}-$name-${deploymentType.name}"

    //==================================================== 람다 설정 ======================================================
    lateinit var lambda: IFunction

    //==================================================== 배치 설정 ======================================================
    lateinit var jobDefinitionArn: String
    lateinit var jobQueueArn: String

    fun create(chains: List<Any>): StateMachine {
        val definition = convertAny(chains)
        val stateMachine = StateMachine(
            stack, "sfn-$logicalName", StateMachineProps.builder()
                .stateMachineName(logicalName)
                .timeout(timeout)
                .definition(definition)
                .build()
        )
        TagUtil.tag(stateMachine, deploymentType)
        return stateMachine
    }

    private fun convertAny(chain: Any): IChainable = when (chain) {
        is Pair<*, *> -> {
            val list = (chain.second as List<Any>).map { convertAny(it) }
            Parallel(
                stack, "$name-${chain.first}", ParallelProps.builder()
                    .resultPath("\$.${chain.first}-result") //resultPath 지정시 원본 +@로 리턴됨. 미지정시 해당 에리어의 모든 결과가 array로 리턴됨
                    .comment("${chain.first}") //comment 가 있어야 순서도에서 예쁘게 보인다
                    .build()
            ).branch(
                *list.toTypedArray() //동시에 개별 실행
            )
        }

        is List<*> -> {
            val list = (chain as List<Any>).map { convertAny(it) }
            list.first().also { first ->
                list.drop(1).forEach { (first as INextable).next(it) }
            }
        }

        is IChainable -> chain //그대로 리턴 (혹시나..)
        is SfnChain -> convert(chain)
        else -> throw IllegalArgumentException("${chain::class} is not required")
    }

    private fun convert(chain: SfnChain): State = when (chain) {
        is SfnLambda -> {
            LambdaInvoke(
                stack, "${chain.name}${chain.suffix}", LambdaInvokeProps.builder()
                    .lambdaFunction(lambda)
                    .payload(
                        TaskInput.fromObject(
                            mapOf(
                                "jobPk" to chain.name, //실행할 잡 이름 (설정과 일치해야함)
                                "${SfnUtil.jobOption}.$" to "$.${SfnUtil.jobOption}",  //기준날짜 등의 커스텀 옵션 (포함된 모든 잡이 같이 사용)
                            )
                        )
                    )
                    .resultPath("\$.${chain.name}-result")
                    .resultSelector(
                        mapOf(
                            "jobName" to chain.name,
                            "statusCode.$" to "$.StatusCode",
                            "body.$" to "$.Payload",
                        )
                    )
                    .comment("${chain.name}")
                    .build()
            )
        }

        is SfnBatch -> {
            BatchSubmitJob(
                stack, "${chain.name}${chain.suffix}", BatchSubmitJobProps.builder()
                    .jobName(chain.name)
                    .jobDefinitionArn(jobDefinitionArn)
                    .jobQueueArn(jobQueueArn)
                    .payload(
                        TaskInput.fromObject(
                            //정해진 키 값들이 args[] 로 매핑됨. -> 각 데이터는 문자열 형식만 가능함
                            mapOf(
                                BatchUtil.BATCH_ARGS01 to obj { "jobPk" to chain.name }.toString(),
                                "${BatchUtil.BATCH_ARGS02}.$" to "$.${SfnUtil.jobOption}",
                            )
                        )
                    )
                    .resultPath("$.${chain.name}-result")
                    .resultSelector(
                        mapOf(
                            "jobName" to chain.name,
                            "statusCode.$" to "$.Status",
                            "StatusReason.$" to "$.StatusReason",
                        )
                    )
                    .comment("${chain.name}")
                    .build()
            )
        }

        is SfnWait -> {
            Wait(
                stack, "${chain.name}${chain.suffix}", WaitProps.builder()
                    .time(WaitTime.timestampPath("$.${chain.name}"))
                    .build()
            )
        }

    }

}