package net.kotlinx.aws.lambda.eventHandler

import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.core.gson.GsonData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * AWS 이벤트브릿지에 달린 이벤트가 전송되는 경우 실행
 * https://ap-northeast-2.console.aws.amazon.com/events/home?region=ap-northeast-2#/registries/aws.events/schemas/aws.events%2540ScheduledJson
 * 이벤트 스키마 dto 매핑 쓰지 않는다.  -> java8 & 잭슨이라서 좋지않다.
 * */
class LambdaEventbridgeScheduleHandler : (GsonData) -> String?, KoinComponent {

    private val log = KotlinLogging.logger {}

    private val eventBus: EventBus by inject()

    override fun invoke(event: GsonData): String? {
        val detailType: String = event["detail-type"].str ?: return null
        if (!detailType.startsWith(SCHEDULED_EVENT)) return null

        //ex) arn:aws:events:ap-northeast-2:653734769926:rule/sin-day_update-dev
        val resourceArn: String = event["resources"][0].str!!
        val eventName: String = resourceArn.substringAfterLast("/")

        eventBus.post(LambdaEventbridgeEvent(eventName))
        return LambdaUtil.OK
    }

    companion object {
        private const val SCHEDULED_EVENT = "Scheduled Event"
    }

}