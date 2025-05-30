package net.kotlinx.aws.firehose.logData

import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonData


/**
 * 로그 데이터 개별 상세
 * @see LogData 필드 설명 참고
 *  */
class LogDataDetail3 {

    @Kdsl
    constructor(block: LogDataDetail3.() -> Unit = {}) {
        apply(block)
    }

    /** 입력시 디폴트값 오버라이드 */
    var memberId: String? = null

    var g1: String = ""

    var g2: String = ""

    var g3: String = ""

    var keyword: String = ""

    var x: GsonData = EMPTY

    var y: GsonData = EMPTY


    companion object {
        val EMPTY = GsonData.empty()
    }

}