package net.kotlinx.aws.lambda.dispatch

import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import net.kotlinx.aws.lambda.LambdaUrlMap
import net.kotlinx.aws.lambda.dispatch.synch.CommandDispatcher
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.kotest.modules.job.DemoJob
import net.kotlinx.okhttp.buildUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class LambdaDispatcherSynchTest : BeSpecHeavy() {

    private val dispatcher by koinLazy<LambdaDispatcher>()

    init {
        initTest(KotestUtil.FAST)

        Given("잡 실행 요청") {
            DemoJob.cnt.get() shouldBe 0

            val input = DemoJob().createEmptyJob()
            Then("잡 실행됨") {
                dispatcher.handleRequest(input)
                DemoJob.cnt.get() shouldBe 1
            }
        }

        Given("ktor(http) 요청") {

            val urlMap = LambdaUrlMap {
                url = "https://xxx.site.com/index".toHttpUrl().buildUrl {
                    addQueryParameter("query", "청바지")
                    addQueryParameter("type", "SM6")
                }
                headers = mapOf("a" to "b")
            }
            log.debug { "urlMap $urlMap" }
            val resp = dispatcher.handleRequest(urlMap, null)
            resp["statusCode"] shouldBe 200
            resp["body"] shouldBe contain("메인 데모 화면입니다")
            resp["body"] shouldBe contain("a=[b]")
            resp["body"] shouldBe contain("query=[청바지]")
        }

        Given("커맨드 실행") {
            val input = obj {
                CommandDispatcher.COMMAND_NAME to "test"
            }
            Then("잡 실행됨") {
                val result = dispatcher.handleRequest(input)
                result["a"] shouldBe "b"
            }
        }
    }

}
