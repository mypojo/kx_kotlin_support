package net.kotlinx.okhttp

import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
import okhttp3.OkHttpClient

/** 간단 데이터 모음집 */
object OkHttpSamples {

    /**
     * 실시간 달러 원 환율 리턴
     * 비동기 버전
     *  */
    suspend fun dollarWonAwait(client: OkHttpClient = koin<OkHttpClient>()): Double {
        val resp: String = client.await {
            url = "https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD"
        }.respText
        return GsonData.parse(resp)[0]["basePrice"].str!!.toDouble()
    }

    /**
     * 실시간 달러 원 환율 리턴
     * 동기 버전  (성능 테스트용)
     *  */
    fun dollarWonFetch(client: OkHttpClient = koin<OkHttpClient>()): Double {
        val resp: String = client.fetch {
            url = "https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD"
        }.respText
        return GsonData.parse(resp)[0]["basePrice"].str!!.toDouble()
    }

}
