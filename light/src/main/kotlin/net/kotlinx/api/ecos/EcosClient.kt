package net.kotlinx.api.ecos

import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.okhttp.await
import net.kotlinx.string.toBigDecimal2
import net.kotlinx.time.toYmd
import okhttp3.OkHttpClient
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 한국국은행 공개 API
 * https://ecos.bok.or.kr/api/#/
 * */
class EcosClient(private val secret: String, private val client: OkHttpClient = koin<OkHttpClient>()) {

    companion object {
        const val HOST = "https://ecos.bok.or.kr/api/StatisticSearch"
    }

    /**
     * 실시간 달러 원 환율 리턴
     * 원래 API는 기간을 입력해 벌크로 리턴받음.
     *  */
    suspend fun dollarWon(date: String = LocalDate.now().toYmd()): BigDecimal {
        val resp = client.await {
            url("$HOST/$secret/json/kr/1/1/731Y001/D/$date/$date/0000001")
        }
        val result = GsonData.parse(resp.respText)
        check(!result.empty) { "${resp.response.code} ${resp.respText}" }

        val item = result["StatisticSearch"]["row"][0]
        return item["DATA_VALUE"].str!!.toBigDecimal2()
    }

}
