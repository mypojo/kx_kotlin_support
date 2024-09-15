package net.kotlinx.awscdk.channel

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
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

    /** 결과 */
    lateinit var iEventBus: IEventBus

    fun create(stack: Stack) {
        val stackId = "${project.profileName}-ev_${logicalName}"
        iEventBus = EventBus(stack, stackId, EventBusProps.builder().eventBusName(logicalName).build())
        TagUtil.tag(iEventBus, deploymentType)
    }

    /**
     * 디폴트 이벤트버스에는 AWS 기본 이벤트들이 다 전파됨
     * 이들중 특정 패턴을 이 커스텀 이벤트버스로 디스패치함
     * 코드 참고용임!
     *  */
    fun fromDefaultEventbus(stack: Stack, eventPattern: EventPattern = EventPatternUtil.AWS_CORE) {
        val ruleName = "${project.profileName}-event_dispatch_${eventBusName}-${suff}"
        val eventDisRule = Rule(
            stack, ruleName, RuleProps.builder()
                .enabled(true)
                .ruleName(ruleName)
                .description("AWS(eventbus) => ${project.profileName}(eventbus) / $deploymentType")
                .targets(listOf(software.amazon.awscdk.services.events.targets.EventBus(iEventBus)))
                .eventPattern(eventPattern)
                .build()
        )
        TagUtil.tag(eventDisRule, deploymentType)
    }

    /**
     * 특정 패턴을 구독해서 target으로 전송
     * ex)                 .eventPattern(eventPattern)
     *                 .targets(listOf(SnsTopic(topic)))
     *
     * 타겟정보 정리
     * SnsTopic -> 정의되지 않은경우 파싱되지 않은 SNS
     * LambdaFunction -> 정의되지 않은경우 데드메시지
     * */
    fun substribe(stack: Stack, name: String, block: RuleProps.Builder.() -> Unit) {
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
        TagUtil.tag(eventDisRule, deploymentType)
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
// TagUtil.deploymentType(discoverer, deploymentType);

// //아카이브 - 특정 기간의 이벤트 리플레이용. 불필요하면 생성하지 않음
// const archiveName = `${THE.projectName}-eventbus_archive-${deploymentType}`
// const archive = new Archive(stack, archiveName, {
//     eventPattern: {
//         source: allEventSources,
//     },
//     sourceEventBus: eventBus,
//     archiveName: archiveName,
//     retention: Duration.days(7 * 2),
// });
// TagUtil.deploymentType(archive, deploymentType);


}