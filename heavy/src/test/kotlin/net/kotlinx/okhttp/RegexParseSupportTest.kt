package net.kotlinx.okhttp

import net.kotlinx.core.text.RegexParseSupport
import net.kotlinx.test.TestRoot
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test

class RegexParseSupportTest : TestRoot(), RegexParseSupport {

    @Test
    fun test() {

        val client = OkHttpClient()
        val resp: String = client.fetch {
            url = "https://www.findip.kr/"
        }.respText!!

        log.trace { "전문 : $resp" }

        resp.find("<head>" to "</footer>")?.let { footer ->
            log.trace { "footer : $footer" }
            footer.findAll("<P" to "</p>").forEach {
                log.debug { " -> $it" }
            }
            log.info { "site : ${footer.extract("이 사이트(" to ")")}" }
            log.info { "title : ${resp.extract("<title>" to "</title>")}" }
        }
    }


}