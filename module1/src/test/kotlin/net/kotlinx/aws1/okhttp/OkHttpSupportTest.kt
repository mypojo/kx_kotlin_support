package net.kotlinx.aws1.okhttp

import mu.KotlinLogging
import net.kotlinx.aws1.AwsInstanceTypeUtil
import net.kotlinx.core1.regex.RegexSet
import net.kotlinx.core2.concurrent.coroutineExecute
import net.kotlinx.core2.test.TestLevel02
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

    /** 코루틴 & 스래드 테스트 */
    @TestLevel02
    fun `http 코루틴 테스트`() {

        (0..5).map {
            suspend{
                log.debug { "작업 $it 시작.." }
                val resp: String = client.await {
                    url = "https://www.findip.kr/"
                }.respText!!
                log.info { "작업 $it 종료" }
                RegexSet.extract("(IP Address): ", "</h2>").toRegex().find(resp)!!.value
            }
        }.coroutineExecute(4).map {
            log.info { "결과 : $it" }
        }

    }


}