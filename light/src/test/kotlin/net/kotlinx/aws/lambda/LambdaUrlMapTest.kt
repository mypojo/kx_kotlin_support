package net.kotlinx.aws.lambda

import net.kotlinx.core.gson.GsonData
import net.kotlinx.test.TestRoot
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test

class LambdaUrlMapTest : TestRoot() {


    @Test
    fun test() {
        val urlMap = LambdaUrlMap {
            url = "https://docs.gradle.org/8.4/userguide/command?a=b&c=dd".toHttpUrl()
        }
        log.info { "map str -> $urlMap" }
        check(urlMap["rawPath"] == "/8.4/userguide/command")
        check(GsonData.fromObj(urlMap)["queryStringParameters"]["c"].str == "dd")

    }

}