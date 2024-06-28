package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.guava.postEvent
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy

data class SchedulerEvent(
    /** 스케쥴 그룹명 */
    val groupName: String,
    /** 스캐줄 명 */
    val scheduleName: String,
) : AwsLambdaEvent

/**
 * AWS 스케쥴러 핸들러
 * 파라메터 옵션조절 가능하지만 일단 바닐라로 사용함
 * */
class SchedulerEventPublisher : LambdaDispatch {

    private val log = KotlinLogging.logger {}

    private val bus by koinLazy<EventBus>()

    companion object {
        const val DETAIL_TYPE = "detail-type"

        /** 이거 이름, 이벤트브릿지하고 동일함 */
        const val SCHEDULED_EVENT = "Scheduled Event"

        const val STARTS_WITH = "arn:aws:scheduler:"
    }


    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        val detailType = input[DETAIL_TYPE].str ?: return null
        if (!detailType.startsWith(SCHEDULED_EVENT)) return null

        //ex) arn:aws:scheduler:ap-northeast-2:99999999:schedule/{groupName}/{scheduleName}
        val resourceArn: String = input["resources"][0].str!!
        if (!resourceArn.startsWith(STARTS_WITH)) return null

        val scheduleInfo = resourceArn.substringAfter("/").split("/")
        check(scheduleInfo.size == 2)
        log.debug { "[event] ARN = $resourceArn  =>  $scheduleInfo" }
        return bus.postEvent { SchedulerEvent(scheduleInfo[0], scheduleInfo[1]) }
    }


}