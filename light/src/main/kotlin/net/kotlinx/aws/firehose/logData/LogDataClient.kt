package net.kotlinx.aws.firehose.logData

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.firehose.IcebergJsonBuilder
import net.kotlinx.aws.firehose.firehose
import net.kotlinx.aws.firehose.putRecordBatch
import net.kotlinx.aws.lazyAwsClient
import net.kotlinx.core.Kdsl
import net.kotlinx.exception.toSimpleString
import net.kotlinx.time.toYmd


/**
 * LogDataHolder 를 기반으로한 로그 전송기
 * 설치되는곳
 * web : spring filter 에 추가
 * job : JobRunner 의 종료 설정에 추가
 * */
class LogDataClient {

    private val log = KotlinLogging.logger {}

    @Kdsl
    constructor(block: LogDataClient.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정파일 ======================================================

    /** AWS 클라이언트 */
    var client: AwsClient by lazyAwsClient()

    /** json 변환기 */
    lateinit var jsonBuilder: IcebergJsonBuilder

    /** 스트리림 명 */
    lateinit var streamName: String

    /** 플젝명 */
    var projectName: String = "default"

    /** 이벤트 구분 */
    lateinit var eventDiv: String

    /**
     * 디폴드 생성 로직.
     * */
    var detail1Factory: () -> LogDataDetail1 = {
        LogDataDetail1(
            basicDate = LogDataHolder.TX_TIME.toYmd(),
            projectName = projectName,
            eventDiv = eventDiv,
            eventId = "${LogDataHolder.EVENT_ID}",
            eventTime = LogDataHolder.TX_TIME,
            instanceType = AwsInstanceTypeUtil.INSTANCE_TYPE,
        )
    }

    /**
     * 필터
     * ex) 특정 조건에서 특정 데이터는 전송하지 않음
     *  */
    var filter: (LogData) -> Boolean = { true }

    //==================================================== 실행로직 ======================================================

    /**
     * 여기서 발생하는 오류는 일단 무시.
     * 디폴트 생성후, 커스텀하게 업데이트 가능
     *  */
    suspend fun putRecord(d2: LogDataDetail2) {
        try {
            val d1 = detail1Factory()
            val datas = LogDataHolder.DATAS.map { d3 ->
                LogData(
                    basicDate = d1.basicDate,
                    projectName = d1.projectName,
                    eventDiv = d1.eventDiv,
                    eventId = d1.eventId,
                    eventTime = d1.eventTime,
                    instanceType = d1.instanceType,

                    eventName = d2.eventName,
                    eventDesc = d2.eventDesc,
                    eventStatus = d2.eventStatus,
                    eventMills = d2.eventMills,
                    metadata = d2.metadata,

                    memberId = d3.memberId ?: d2.defaultMemberId, //일반적으로 d2 값이지만, d3로 개별 커스텀 가능
                    g1 = d3.g1,
                    g2 = d3.g2,
                    g3 = d3.g3,
                    keyword = d3.keyword,
                    x = d3.x,
                    y = d3.y,
                )
            }.filter(filter)

            if (datas.isEmpty()) return

            val jsons = datas.map { jsonBuilder.build(it) }
            client.firehose.putRecordBatch(streamName, jsons)

        } catch (e: Throwable) {
            log.warn { "이벤트 기록중 예외발생 : ${e.toSimpleString()}" }
        } finally {
            LogDataHolder.remove()
        }
    }


}