package net.kotlinx.okhttp

import net.kotlinx.koin.Koins.koin
import net.kotlinx.regex.RegexSet
import okhttp3.OkHttpClient

object OkHttpDemo {


    /** 아웃바운드 IP를 간단히 리턴해준다.  */
    fun findOutboundIp(client: OkHttpClient = koin<OkHttpClient>()): String {
        val resp: String = client.fetch {
            url = "https://www.findip.kr/"
        }.respText
        return RegexSet.extract("(IP Address): ", "</h2>").toRegex().find(resp)!!.value
    }

    /** 이미지 등의 용량을 간단히 리턴해준다.  */
    suspend fun contentLength(url: String, client: OkHttpClient = koin<OkHttpClient>()): Long {
        val resp = client.await {
            this.method = "HEAD"
            this.url = url
        }
        return resp.response.header("Content-Length")!!.toLong()
    }


}
