package net.kotlinx.aws.athena

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.time.LocalDateTime

internal class AthenaUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("AthenaUtil") {
            Then("스키마출력") {
                val text = AthenaUtil.toSchema(AutobidRankLog::class).map { "${it.key} ${it.value}" }.joinToString(",\n")
                log.info { "스키마 \n$text" }
            }
        }
    }

    /**
     * 임시 로그 객체
     */
    private class AutobidRankLog {

        /** 기준날짜 */
        var basicDate: String = ""

        /** 광고주명(크롤링시 타이틀) */
        var advName: String = ""

        /** 키워드명 */
        var kwdName: String = ""

        /** 도메인(경쟁업체 도메인) */
        var domain: String = ""

        /** 디바이스 */
        var device: String = ""

        /** 시간(크롤링 시간) */
        var eventTime: LocalDateTime = LocalDateTime.now()

        /** 현재 순위 */
        var nowRank: Long = 0L

    }

}