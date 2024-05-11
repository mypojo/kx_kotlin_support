package net.kotlinx.okhttp

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import okhttp3.HttpUrl.Companion.toHttpUrl

class OkHttpUrlBuilderSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("OkHttpUrlSupport") {
            Then("기존 URL을 수정") {
                val url = "https://naver.com?a=b&c=d"

                val updated = url.toHttpUrl().build {
                    addQueryParameter("query", "영감님")
                    addQueryParameter(
                        mapOf(
                            "new" to "n1",
                            "old" to "n2",
                        )
                    )
                }
                log.debug { "변경전 : $url" }
                log.debug { "변경후 : $updated" }
                updated shouldBe "https://naver.com/?a=b&c=d&query=%EC%98%81%EA%B0%90%EB%8B%98&new=n1&old=n2"
            }
        }
    }
}