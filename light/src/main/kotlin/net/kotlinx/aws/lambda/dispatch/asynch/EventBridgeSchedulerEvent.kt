package net.kotlinx.aws.lambda.dispatch.asynch


/**
 * 람다 페이로드는 detail 로 넘어옴
 * */
data class EventBridgeSchedulerEvent(private val event: EventBridgeJson) : EventBridge by event {

    /** 스케쥴 그룹명 */
    val groupName: String

    /** 스캐줄 명 */
    val scheduleName: String

    init {
        //ex) arn:aws:scheduler:ap-northeast-2:99999999:schedule/{groupName}/{scheduleName}
        val scheduleInfo = resources.first().substringAfter("/").split("/")
        check(scheduleInfo.size == 2)
        groupName = scheduleInfo[0]
        scheduleName = scheduleInfo[1]
    }


}


