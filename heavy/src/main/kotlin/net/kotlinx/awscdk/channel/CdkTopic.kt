package net.kotlinx.awscdk.channel

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.basic.TagUtil
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.AnyPrincipal
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.sns.ITopic
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sns.TopicProps

/** enum */
class CdkTopic(val topicName: String) : CdkEnum {

    override val logicalName: String
        get() = "${projectName}-topic-${topicName}-${suff}"

    /** 전체 오픈할 기능 */
    var allOpenActions = listOf(
        "SNS:Publish",
        "SNS:Subscribe", //이거는 논란의  여지가 있을 수 있음
    )

    /** 결과 */
    lateinit var iTopic: ITopic

    fun create(stack: Stack, block: TopicProps.Builder.() -> Unit = {}): CdkTopic {
        iTopic = Topic(stack, logicalName, TopicProps.builder().topicName(logicalName).apply(block).build())
        TagUtil.tagDefault(iTopic)

        //중요!! 정책을 정확하게 줘야 작동함!!
        //기본 정책. AWS 소유자 전체 오픈
        iTopic.addToResourcePolicy(
            PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .resources(listOf(iTopic.topicArn))
                .principals(listOf(AnyPrincipal()))
                .actions(
                    listOf(
                        //접두어 대소문자 둘다 가능
                        "SNS:Publish",
                        "SNS:Subscribe",
                        "SNS:GetTopicAttributes",
                        "SNS:SetTopicAttributes",
                        "SNS:AddPermission",
                        "SNS:RemovePermission",
                        "SNS:DeleteTopic",
                        "SNS:ListSubscriptionsByTopic",
                    )
                )
                .conditions(
                    mapOf(
                        "StringEquals" to mapOf(
                            "AWS:SourceOwner" to awsConfig.awsId
                        )
                    )
                ).build()
        )
        //디폴트 정책만 주면 id로 StringEquals 제한이 있어서 pipeline이 호출 못함. -> 기본기능 2개를 전체 오픈함
        iTopic.addToResourcePolicy(
            PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .resources(listOf(iTopic.topicArn))
                .principals(listOf(AnyPrincipal()))
                .actions(allOpenActions)
                .build()
        )
        return this
    }

    fun load(stack: Stack): ITopic {
        val arn = "arn:aws:sns:ap-northeast-2:${this.awsConfig.awsId}:${logicalName}"
        return Topic.fromTopicArn(stack, logicalName, arn)
    }
}

