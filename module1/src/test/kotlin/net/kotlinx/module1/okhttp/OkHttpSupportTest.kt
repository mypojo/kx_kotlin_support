package net.kotlinx.module1.okhttp

import mu.KotlinLogging
import net.kotlinx.aws1.AwsInstanceTypeUtil
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.io.File


internal class OkHttpSupportTest {

    private val log = KotlinLogging.logger {}

    private val client = OkHttpClient()

    @Test
    fun `기본테스트`() {
        val resp = client.fetch {
            url = "https://publicobject.com/helloworld.txt"
        }
        println(resp.respText)
    }

    @Test
    fun `다운로드_캐시`() {

        val file = File(AwsInstanceTypeUtil.instanceType.root, "demo.jpg")
        file.delete()
        val url = "http://imgep.xxx.co.kr/productimg/image/img9/29/300011929_FI57778.jpg"

        val resp1 = client.download(file) {
            this.url = url
        }
        val lastModified = resp1.lastModified
        log.info { "code : ${resp1.response.code} / 파일크기 : ${file.length()} / lastModified : ${resp1.lastModified}" }

        //Wed, 22 Aug 2018 09:07:06 GMT
        val resp2 = client.download(file) {
            this.url = url
            dirtyCheck(lastModified)
        }
        log.info { "code : ${resp2.response.code} / 파일크기 : ${file.length()} / lastModified : ${resp2.lastModified}" }
    }


}