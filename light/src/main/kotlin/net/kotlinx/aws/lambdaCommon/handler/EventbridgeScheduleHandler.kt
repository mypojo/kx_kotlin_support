package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import mu.KotlinLogging
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.json.gson.GsonData


/**
 * EventBridge 스케줄링 등록 ->  람다 호출
 * ex) 잡 실행, 특정 로직 실행 등..
 * */
class EventbridgeScheduleHandler(
    /**
     * arn 기반으로 트리거
     * ex) myproject-day_update-dev
     * ex) {프로젝트명}_{jobDiv}-{deployment_div}
     * */
    private val block: suspend (eventName: String) -> Unit
) : LambdaLogicHandler {

    private val log = KotlinLogging.logger {}

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        val detailType = input[DETAIL_TYPE].str ?: return null
        if (!detailType.startsWith(SCHEDULED_EVENT)) return null

        //ex) arn:aws:events:ap-northeast-2:xxx:rule/xx-day_update-dev
        val resourceArn: String = input["resources"][0].str!!
        if (!resourceArn.startsWith(STARTS_WITH)) return null

        val eventName: String = resourceArn.substringAfterLast("/")
        log.debug { "[event] ARN = $resourceArn  =>  $eventName" }

        block(eventName)
        return eventName
    }

    companion object {
        const val DETAIL_TYPE = "detail-type"
        const val SCHEDULED_EVENT = "Scheduled Event"

        const val STARTS_WITH = "arn:aws:events:"
    }


}