package net.kotlinx.okhttp

import net.kotlinx.kotest.BeSpecLog
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test

class OkHttpUrlBuilderSupportKtTest : BeSpecLog() {
    init {
        @Test
        fun test() {

            val url = "https://naver.com?a=b&c=d"

            val updated = url.toHttpUrl().build {
                addQueryParameter("query", "영감님")
                addQueryParameter(
                    mapOf(
                        "new" to "xx"
                    )
                )
            }

            println(updated)


        }
    }
}