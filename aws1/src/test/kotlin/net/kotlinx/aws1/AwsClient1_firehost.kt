package net.kotlinx.aws1

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.firehose.putRecord
import net.kotlinx.core1.time.TimeUtil
import net.kotlinx.core1.time.toYmd
import net.kotlinx.core2.gson.GsonSet
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AwsClient1_firehost {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    class DemoLog {

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
        var nowRank: Int = 0

    }


    @Test
    fun run() = runBlocking {

        TimeUtil.initTimeZone()

        val now = LocalDateTime.now()

        for (i in 0..6) {
            val rankLog = DemoLog().apply {
                basicDate = now.toLocalDate().toYmd()
                kwdName = "청바지"
//                kwdName = "반바지"
                advName = "광고주 $i"
                domain = "도메인 $i"
                eventTime = now
                nowRank = i + 1
            }
            val json = GsonSet.TABLE_UTC.toJson(rankLog)
            println(json)
            putRecord(json)
        }

    }

    private suspend fun putRecord(json: String) {
        aws.firehose.putRecord("autobid_rank", json)
    }

}