package net.kotlinx.time

import net.kotlinx.core.Kdsl
import java.time.LocalDateTime
import java.time.ZoneId


/**
 * UTC 변환기
 * */
class UtcConverter {

    @Kdsl
    constructor(block: UtcConverter.() -> Unit = {}) {
        apply(block)
    }

    /** 존 정보 */
    var zoneId: ZoneId = TimeUtil.SEOUL

    /** 변환 포맷 */
    var timeFormat: TimeFormat = TimeFormat.ISO_INSTANT_SEOUL

    /**
     * 정해진 존의 로컬시간을 문자열로 변환
     *  */
    fun toText(time: LocalDateTime): String = timeFormat[time.atZone(zoneId)]

    /**
     * 읽을때는 UTC 고정
     *  */
    fun fromText(text: String): LocalDateTime = timeFormat.toZonedDateTime(text).toLocalDateTime()

    companion object {

        /**
         * athena timestamp에서 사용
         * ex) kinesis로 athena 아이스버그 테이블 입력
         * */
        val ISO_INSTANT = UtcConverter {
            //기본세팅 사용
        }

        /**
         * eventBridge 에 사용
         * */
        val ISO_OFFSET = UtcConverter {
            timeFormat = TimeFormat.ISO_OFFSET
        }

    }

}