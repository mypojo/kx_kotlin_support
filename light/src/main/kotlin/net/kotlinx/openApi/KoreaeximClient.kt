package net.kotlinx.openApi

import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.okhttp.await
import net.kotlinx.string.toBigDecimal2
import net.kotlinx.time.toYmd
import okhttp3.OkHttpClient
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 한국 수출입은행 공개 API
 * https://www.koreaexim.go.kr/ir/HPHKIR020M01?apino=2&viewtype=C&searchselect=&searchword=
 * */
class KoreaeximClient(private val secret: String, private val client: OkHttpClient = koin<OkHttpClient>()) {

    companion object {
        const val HOST = "https://www.koreaexim.go.kr/site/program/financial/exchangeJSON"
    }

    /**
     * 실시간 달러 원 환율 리턴
     * 비동기 버전
     * @param date  주말기준 3일 전 해야 보통 나옴
     *  */
    suspend fun dollarWon(date: String = LocalDate.now().minusDays(3).toYmd()): BigDecimal {
        val resp = client.await {
            url(HOST) {
                addQueryParameter("authkey", secret)
                addQueryParameter("searchdate", date)
                addQueryParameter("data", "AP01")
            }
        }
        val result = GsonData.parse(resp.respText)
        check(!result.empty)
        return result.first { it["cur_unit"].str == "USD" }["deal_bas_r"].str!!.toBigDecimal2()
    }

}
