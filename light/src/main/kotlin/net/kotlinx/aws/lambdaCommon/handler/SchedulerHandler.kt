package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import mu.KotlinLogging
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.json.gson.GsonData


/**
 * AWS 스케쥴러 핸들러
 * 파라메터 옵션조절 가능하지만 일단 바닐라로 사용함
 * */
class SchedulerHandler(

    private val block: suspend (groupName: String, scheduleName: String) -> Unit
) : LambdaLogicHandler {

    private val log = KotlinLogging.logger {}

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        val detailType = input[DETAIL_TYPE].str ?: return null
        if (!detailType.startsWith(SCHEDULED_EVENT)) return null

        //ex) arn:aws:scheduler:ap-northeast-2:99999999:schedule/{groupName}/{scheduleName}
        val resourceArn: String = input["resources"][0].str!!
        if (!resourceArn.startsWith(STARTS_WITH)) return null

        val scheduleInfo = resourceArn.substringAfter("/").split("/")
        check(scheduleInfo.size == 2)
        log.debug { "[event] ARN = $resourceArn  =>  $scheduleInfo" }

        block(scheduleInfo[0], scheduleInfo[1])
        return scheduleInfo[1]
    }

    companion object {
        const val DETAIL_TYPE = "detail-type"

        /** 이거 이름, 이벤트브릿지하고 동일함 */
        const val SCHEDULED_EVENT = "Scheduled Event"

        const val STARTS_WITH = "arn:aws:scheduler:"
    }


}