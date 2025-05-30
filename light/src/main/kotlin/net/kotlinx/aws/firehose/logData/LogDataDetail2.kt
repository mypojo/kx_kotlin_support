package net.kotlinx.aws.firehose.logData

import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonData


/**
 * 로그 데이터 개별 상세
 * @see LogData 필드 설명 참고
 *  */
class LogDataDetail2 {

    @Kdsl
    constructor(block: LogDataDetail2.() -> Unit = {}) {
        apply(block)
    }

    /** MemberId 가   null 로 입력되는경우 이것으로 대체 */
    var defaultMemberId: String = DEFAULT_MEMBER_ID

    lateinit var eventName: String

    lateinit var eventDesc: String

    lateinit var eventStatus: String

    var eventMills: Long = -1

    var metadata: GsonData = EMPTY

    companion object {
        val EMPTY = GsonData.empty()
        const val DEFAULT_MEMBER_ID = "system"
    }

}