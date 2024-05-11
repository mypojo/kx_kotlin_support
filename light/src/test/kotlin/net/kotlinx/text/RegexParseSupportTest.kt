package net.kotlinx.text

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.okhttp.fetch
import net.kotlinx.regex.RegexParseSupport
import okhttp3.OkHttpClient

class RegexParseSupportTest : BeSpecLight() {

    init {
        initTest(KotestUtil.SLOW)

        Given("RegexParseSupport") {

            val client = OkHttpClient()

            Then("간단 정규식 파싱 -> 푸터 읽기") {
                val resp: String = client.fetch {
                    url = "https://www.findip.kr/"
                }.respText!!

                log.trace { "전문 : $resp" }

                RegexParseSupport.template {
                    val footer = resp.find("<head>" to "</footer>")!!
                    log.trace { "footer : $footer" }

                    log.info { "site : ${footer.extract("이 사이트(" to ")")}" }
                    log.info { "title : ${resp.extract("<title>" to "</title>")}" }
                    footer.extract("이 사이트(" to ")") shouldBe "findip.kr"
                }
            }
        }
    }
}