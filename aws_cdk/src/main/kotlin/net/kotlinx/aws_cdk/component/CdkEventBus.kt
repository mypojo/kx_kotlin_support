package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.EventPatternUtil
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core1.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.*


/**
 * 디폴트 말고 새로운 이벤트 버스 생성
 * */
class CdkEventBus(
    val project: CdkProject,
    val deploymentType: DeploymentType,
) {

    lateinit var iEventBus: IEventBus

    fun create(stack: Stack) {
        val eventBusName = "${project.projectName}-eventbus-${deploymentType}"
        iEventBus = EventBus(stack, eventBusName, EventBusProps.builder().eventBusName(eventBusName).build())
        TagUtil.tag(iEventBus!!, deploymentType)
    }

    /**
     * 디폴트 이벤트버스의 특정 패턴을 이 커스텀 이벤트버스로 연결
     * 코드 참고용임!
     *  */
    fun link(stack: Stack, eventPattern: EventPattern = EventPatternUtil.of(DEFAULT_EVENT_LIST)) {
        val eventDisRuleName = "${project.projectName}-event_dispatch-${deploymentType}"
        val eventDisRule = Rule(
            stack, eventDisRuleName, RuleProps.builder()
                .enabled(true)
                .ruleName(eventDisRuleName)
                .description("AWS(eventbus) => ${project.projectName}(eventbus) / ${this.deploymentType}")
                .targets(listOf(software.amazon.awscdk.services.events.targets.EventBus(iEventBus!!)))
                .eventPattern(eventPattern)
                .build()
        )
        TagUtil.tag(eventDisRule, deploymentType)
    }


    companion object {
        /** 참고 샘플 */
        val DEFAULT_EVENT_LIST = listOf(
            "aws.ecs",
            "aws.sns",
            "aws.codecommit",
            "aws.autoscaling",
        )
    }


}