package net.kotlinx.aws_cdk.component.sfn

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core1.DeploymentType
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.EventPattern
import software.amazon.awscdk.services.events.Rule
import software.amazon.awscdk.services.events.RuleProps
import software.amazon.awscdk.services.events.targets.SnsTopic
import software.amazon.awscdk.services.events.targets.SnsTopicProps
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.sns.ITopic
import software.amazon.awscdk.services.sqs.IQueue
import software.amazon.awscdk.services.stepfunctions.*

/**
 * ID 중복 최소한만 고려 (화면에 안예쁘게 나옴)
 * ex)  {"jobOption":{"sfnId":"9a25f502-588c-42e6-8be5-00955f1a60ac","basicDate":"20230414"},"jobOptionText":"{\"sfnId\":\"9a25f502-588c-42e6-8be5-00955f1a60ac\",\"basicDate\":\"20230414\"}"}
 * */
class CdkSfn(
    val project: CdkProject,
    val stack: Stack,
    val name: String,
    val timeout: Duration = Duration.hours(8), //새벽 기준으로 출근 할때까지
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = DeploymentType.dev

    override val logicalName: String = "${project.projectName}-$name-${deploymentType.name}"

    //==================================================== 람다 설정 ======================================================
    lateinit var lambda: IFunction

    //==================================================== 배치 설정 ======================================================
    lateinit var jobDefinitionArn: String
    lateinit var jobQueueArn: String

    /** 없으면 기본생성해주긴 하지만, 중간에 추가 권한 줘야하면 오류나니 그냥 ADMIN 권한 넣어주자.  */
    var iRole: IRole? = null

    lateinit var stateMachine: StateMachine

    fun create(chains: List<Any>) {
        val definition = convertAny(chains)
        stateMachine = StateMachine(
            stack, "sfn-$logicalName", StateMachineProps.builder()
                .stateMachineName(logicalName)
                .timeout(timeout)
                .definition(definition)
                .role(iRole)
                .build()
        )
        TagUtil.tag(stateMachine, deploymentType)
    }

    /**  예외 처리 -> SNS */
    fun onErrorHandle(topic: ITopic, dlq: IQueue) {
        val ruleName = "${logicalName}-faileAlert"
        val rule = Rule(
            stack, ruleName, RuleProps.builder()
                .ruleName(ruleName)
                .description("$logicalName fail alert")
                .targets(listOf(SnsTopic(topic, SnsTopicProps.builder().deadLetterQueue(dlq).build())))
                .eventPattern(
                    EventPattern.builder()
                        .source(listOf("aws.states"))
                        .detailType(listOf("Step Functions Execution Status Change"))
                        .detail(
                            mapOf(
                                "stateMachineArn" to listOf(stateMachine.stateMachineArn),
                                "status" to listOf("FAILED")
                            )
                        )
                        .build()
                )
                .build()
        )
        TagUtil.tag(rule, deploymentType)
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
            //순서대로 체인을 이어준다.
            for (i in 0 until list.size - 1) {
                val current = list[i]
                val next = list[i + 1]
                (current as INextable).next(next)
            }
            list.first() //첫번째 객체를 리턴
        }

        is IChainable -> chain //그대로 리턴 (혹시나..)
        is SfnChain -> chain.convert(this)
        else -> throw IllegalArgumentException("${chain::class} is not required")
    }

}