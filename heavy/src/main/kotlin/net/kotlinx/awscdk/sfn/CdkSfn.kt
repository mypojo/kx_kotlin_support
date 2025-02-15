package net.kotlinx.awscdk.sfn

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.EventPattern
import software.amazon.awscdk.services.events.Rule
import software.amazon.awscdk.services.events.RuleProps
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.events.targets.LambdaFunctionProps
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.sqs.IQueue
import software.amazon.awscdk.services.stepfunctions.DefinitionBody
import software.amazon.awscdk.services.stepfunctions.IChainable
import software.amazon.awscdk.services.stepfunctions.StateMachine
import software.amazon.awscdk.services.stepfunctions.StateMachineProps

/**
 * ID 중복 최소한만 고려 (화면에 안예쁘게 나옴)
 * ex)  {"jobOption":{"sfnId":"9a25f502-588c-42e6-8be5-00955f1a60ac","basicDate":"20230414"},"jobOptionText":"{\"sfnId\":\"9a25f502-588c-42e6-8be5-00955f1a60ac\",\"basicDate\":\"20230414\"}"}
 *
 * 내부 객체들을 inner class 로 했어야 했다.. 아쉽네.
 * */
class CdkSfn : CdkInterface {

    @Kdsl
    constructor(block: CdkSfn.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 속성들 ======================================================
    lateinit var name: String
    lateinit var stack: Stack

    override val logicalName: String
        get() = "${projectName}-$name-${suff}"

    /** 새벽 기준으로 출근 할때까지 */
    var timeout: Duration = Duration.hours(6)

    /** 없으면 기본생성해주긴 하지만, 중간에 추가 권한 줘야하면 오류나니 그냥 ADMIN 권한 넣어주자.  */
    lateinit var iRole: IRole

    //==================================================== 람다 설정 ======================================================
    lateinit var lambda: IFunction

    //==================================================== 배치 설정 ======================================================
    lateinit var jobDefinitionArn: String
    lateinit var jobQueueArn: String

    //==================================================== 단축 ======================================================
    fun lambda(name: String, block: CdkSfnLambda.() -> Unit = {}) = CdkSfnLambda(this, name).apply(block).convert()
    fun wait(name: String, block: CdkSfnWait.() -> Unit = {}) = CdkSfnWait(this, name).apply(block).convert()
    fun choice(name: String, block: CdkSfnChoice.() -> Unit = {}) = CdkSfnChoice(this, name).apply(block).convert()
    fun mapInline(name: String, block: CdkSfnMapInline.() -> Unit = {}) = CdkSfnMapInline(this, name).apply(block).convert()
    //==================================================== 작업 ======================================================

    /** 결과 */
    lateinit var stateMachine: StateMachine

    /** 처음(시작) 객체가 입력되어야 한다 */
    fun create(vararg definitions: IChainable) {
        stateMachine = StateMachine(
            stack, "sfn-$logicalName", StateMachineProps.builder()
                .stateMachineName(logicalName)
                .timeout(timeout)
                .definitionBody(DefinitionBody.fromChainable(definitions.toList().join()))
                .role(iRole)
                .build()
        )
        TagUtil.tagDefault(stateMachine)
    }

    /**
     * 이벤트 변경시 SNS 호출
     *  */
    fun onEventHandle(dlq: IQueue) {
        val ruleName = "${logicalName}-statusChange"
        val rule = Rule(
            stack, ruleName, RuleProps.builder()
                .ruleName(ruleName)
                .description("$logicalName statusChange to lambda")
                .targets(listOf(LambdaFunction(lambda, LambdaFunctionProps.builder().deadLetterQueue(dlq).build())))
                .eventPattern(
                    EventPattern.builder()
                        .source(listOf("aws.states"))
                        .detailType(listOf("Step Functions Execution Status Change"))
                        .detail(
                            mapOf(
                                "stateMachineArn" to listOf(stateMachine.stateMachineArn),
                                //https://docs.aws.amazon.com/ko_kr/step-functions/latest/dg/eventbridge-integration.html
                                //"RUNNING | SUCCEEDED | FAILED | TIMED_OUT | ABORTED | PENDING_REDRIVE"
                                "status" to listOf("SUCCEEDED", "FAILED", "TIMED_OUT", "ABORTED", "PENDING_REDRIVE")
                            )
                        )
                        .build()
                )
                .build()
        )
        TagUtil.tagDefault(rule)
    }


}