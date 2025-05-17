package net.kotlinx.api.ecos

import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.okhttp.await
import net.kotlinx.string.toBigDecimal2
import net.kotlinx.time.toYmd
import okhttp3.OkHttpClient
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 한국은행 공개 API
 * https://ecos.bok.or.kr/api/#/
 * */
class EcosClient(private val secret: String, private val client: OkHttpClient = koin<OkHttpClient>()) {

    companion object {
        const val HOST = "https://ecos.bok.or.kr/api/StatisticSearch"
        private val log = KotlinLogging.logger {}
    }

    /**
     * 실시간 달러 원 환율 리턴
     * 원래 API는 기간을 입력해 벌크로 리턴받음.
     * 오늘이나 휴일을 입력하면 데이터가 없다고 나올 수 잇음 -> 안전하게 7일전으로 구함
     *  */
    suspend fun dollarWon(date: String = LocalDate.now().minusDays(7).toYmd()): BigDecimal {
        val resp = client.await {
            url("$HOST/$secret/json/kr/1/1/731Y001/D/$date/$date/0000001")
        }
        val result = GsonData.parse(resp.respText)
        check(!result.empty) { "${resp.response.code} ${resp.respText}" }

        try {
            val item = result["StatisticSearch"]["row"][0]
            return item["DATA_VALUE"].str!!.toBigDecimal2()
        } catch (e: Exception) {
            log.error { "[$date] 데이터 이상!! 날짜를 변경해보세요  \n${result.toPreety()}" }
            throw e
        }
    }

}
