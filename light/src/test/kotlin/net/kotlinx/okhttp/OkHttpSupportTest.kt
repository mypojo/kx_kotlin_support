package net.kotlinx.okhttp

import io.kotest.matchers.shouldBe
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.system.ResourceHolder
import okhttp3.OkHttpClient


internal class OkHttpSupportTest : BeSpecLight() {

    init {
        initTest(KotestUtil.SLOW)

        Given("OkHttpClient") {
            val client = koin<OkHttpClient>()

            Then("기본테스트") {
                val resp = client.fetch {
                    url = "https://publicobject.com/helloworld.txt"
                }
                println(resp.respText)
            }

            When("파일 다운로드시 - 캐시 적용") {
                val file = ResourceHolder.getWorkspace().slash("kotest").slash("demo.jpg")
                file.delete()
                val url = "https://flexible.img.hani.co.kr/flexible/normal/970/777/imgdb/resize/2019/0926/00501881_20190926.JPG" //이미지는 아무거나

                var lastModified: String? = null

                Then("첫번째 다운로드는 성공") {
                    val resp = client.download(file) {
                        this.url = url
                    }
                    lastModified = resp.lastModified
                    log.info { "code : ${resp.response.code} / 파일크기 : ${file.length()} / lastModified : ${resp.lastModified}" }
                    resp.response.code shouldBe 200
                }

                Then("두번째 다운로드는 캐시 히트해서 스킵 됨") {
                    val resp = client.download(file) {
                        this.url = url
                        dirtyCheck(lastModified!!)
                    }
                    log.info { "code : ${resp.response.code} / 파일크기 : ${file.length()} / lastModified : ${resp.lastModified}" }
                    resp.response.code shouldBe 304

                    file.delete()
                }
            }


        }
    }


}