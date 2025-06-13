package net.kotlinx.awscdk.channel

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.awscdk.iam.IamPolicyResourceUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.*


/**
 * 디폴트 말고 새로운 이벤트 버스 생성
 * */
class CdkEventBus : CdkInterface {

    @Kdsl
    constructor(block: CdkEventBus.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "${eventBusName}-${suff}"

    /** 이벤트버스 명 */
    lateinit var eventBusName: String

    /** 이 이벤트버스에 put할 수 있는 계정들. 자주 사용해서 여기 넣음 */
    var putAccounts: List<String> = emptyList()

    /** 결과 */
    lateinit var iEventBus: IEventBus

    fun create(stack: Stack) {
        val stackId = "${projectName}-ev_${logicalName}"
        iEventBus = EventBus(stack, stackId, EventBusProps.builder().eventBusName(logicalName).build())
        TagUtil.tagDefault(iEventBus)

        //경고!! 아래 간단 구문이 작동하지 않는다.. diff해도 반응이 없음. 그래서 수동 적용
        //iEventBus.grantPutEventsTo(AccountPrincipal(it))
        if (putAccounts.isNotEmpty()) {
            val policy = IamPolicyResourceUtil.forAccount(
                putAccounts,
                listOf(this.iEventBus.eventBusArn),
                listOf("events:PutEvents")
            )
            policy.sid = "account_policy-" + this.logicalName
            (iEventBus as EventBus).addToResourcePolicy(policy)
        }
    }

    /**
     * 디폴트 이벤트버스에는 AWS 기본 이벤트들이 다 전파됨
     * 이들중 특정 패턴을 이 커스텀 이벤트버스로 디스패치함
     * 코드 참고용임!
     *  */
    fun fromDefaultEventbus(stack: Stack, eventPattern: EventPattern = EventPatternUtil.AWS_CORE) {
        val ruleName = "${projectName}-event_dispatch_${eventBusName}-${suff}"
        val eventDisRule = Rule(
            stack, ruleName, RuleProps.builder()
                .enabled(true)
                .ruleName(ruleName)
                .description("AWS(eventbus) => ${projectName}(eventbus) / $deploymentType")
                .targets(listOf(software.amazon.awscdk.services.events.targets.EventBus(iEventBus)))
                .eventPattern(eventPattern)
                .build()
        )
        TagUtil.tagDefault(eventDisRule)
    }

    /**
     * 특정 패턴을 구독해서 target으로 전송
     * ex)                 .eventPattern(eventPattern)
     *                 .targets(listOf(SnsTopic(topic)))
     *
     * 타겟정보 정리
     * SnsTopic -> 정의되지 않은경우 파싱되지 않은 SNS
     * LambdaFunction -> 정의되지 않은경우 데드메시지
     *
     * 주의!! Role을 안넣어도 됨. 이경우 기본 Role이 자동으로 생성되서 입력됨
     * */
    fun subscribe(stack: Stack, name: String, block: RuleProps.Builder.() -> Unit) {
        val ruleName = "${name}-${suff}"
        val eventDisRule = Rule(
            stack, ruleName,
            RuleProps.builder()
                .enabled(true)
                .ruleName(ruleName)
                .description("eventbridge substribe $name-$suff")
                .eventBus(iEventBus)
                .apply(block)
                .build(),
        )
        TagUtil.tagDefault(eventDisRule)
    }

    /**
     * GB당 0.1$ 정도 나옴
     * 아래 링크 참고
     * https://aws.amazon.com/ko/eventbridge/pricing/
     *
     * ex) 10일 오후 3~4시에 입력된 이벤트를 다시 보고싶음 -> 리플레이 만들어서 로깅 rule 적용 -> 클라우드와치로 로깅됨
     *
     * 로깅 rule 관련은 아래 링크 참고. "replay-name" 이라는 추가 필드로 필터링 가능
     * https://aws.amazon.com/ko/blogs/compute/archiving-and-replaying-events-with-amazon-eventbridge/
     * */
    fun archive(stack: Stack, retentionDays: Int = 30) {
        val logicalName = "${projectName}-${eventBusName}_archive-${suff}"
        val archive = CfnArchive.Builder.create(stack, logicalName)
            .archiveName(logicalName)
            .sourceArn(iEventBus.eventBusArn)
            .retentionDays(retentionDays) // 보존 기간 설정 (30일)
            .build()
        TagUtil.tagDefault(archive)
    }

    companion object {

        /**
         * 디폴트 이벤트버스를 구독할때 사용함 (이벤트버스를 따로 지정하지 않음)
         * */
        fun subscribe(stack: Stack, ruleName: String, block: RuleProps.Builder.() -> Unit) {
            val eventDisRule = Rule(
                stack, ruleName,
                RuleProps.builder()
                    .enabled(true)
                    .ruleName(ruleName)
                    .apply(block)
                    .build(),
            )
            TagUtil.tagDefault(eventDisRule)
        }

    }

    //커스텀 정책이 피요할때 (외부 이벤트 주입 등.)
// new CfnEventBusPolicy(stack, eventBusName+'-policy', {
//     statementId: eventBusName+'-sid',
//     action: 'events:PutEvents',
//     eventBusName: eventBus.eventBusName,
//     principal: THE.awsId.toString(),
// });

// //디스커버 - 별도의 커스텀 이벤트 포맷을 외부 업체에 공개할때 사용
// const discoverName = `${THE.projectName}-eventbus_discover-${deploymentType}`
// const discoverer = new CfnDiscoverer(stack, discoverName, {
//     sourceArn: eventBus.eventBusArn,
//     crossAccount: false,
//     description: discoverName,
// });
// TagUtil.deploymentType(discoverer);


}