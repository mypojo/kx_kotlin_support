package net.kotlinx.aws.okhttp

import net.kotlinx.core.gson.GsonData
import okhttp3.OkHttpClient

/** 간단 데이터 모음집 */
object OkHttpSamples {

    /** 실시간 달러 원 환율 리턴  */
    fun dollarWon(client: OkHttpClient = OkHttpClient()): Double {
        val resp: String = client.fetch {
            url = "https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD"
        }.respText
        println(GsonData.parse(resp)[0]["basePrice"].str)
        return GsonData.parse(resp)[0]["basePrice"].str!!.toDouble()
    }


}
