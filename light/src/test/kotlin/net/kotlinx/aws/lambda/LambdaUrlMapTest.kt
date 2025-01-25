package net.kotlinx.aws.lambda

import io.kotest.matchers.shouldBe
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import okhttp3.HttpUrl.Companion.toHttpUrl

class LambdaUrlMapTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("LambdaUrlMap") {
            Then("Lambda to") {
                val urlMap = LambdaUrlMap {
                    url = "https://docs.gradle.org/8.4/userguide/command?a=bb&c=dd".toHttpUrl()
                }
                log.info { " => $urlMap" }
                urlMap["rawPath"] shouldBe "/8.4/userguide/command"
                GsonData.fromObj(urlMap)["queryStringParameters"]["c"].str shouldBe "dd"

                val gson = GsonData.fromObj(urlMap)
                log.debug { "gson -> $gson" }
                gson.isObject shouldBe true
                gson["queryStringParameters"].toMap() shouldBe mapOf("a" to "bb", "c" to "dd")
            }

        }
    }
}