package net.kotlinx.ktor.server

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.ktor.server.app.configureRouting

class KtorApplicationUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("기본 테스트") {
            Then("간단 실행") {
                testApplication {
                    application {
                        configureRouting()
                    }
                    client.get("/").apply {
                        status shouldBe HttpStatusCode.OK
                        val text = bodyAsText()
                        text shouldBe contain("메인 데모 화면입니다")
                    }
                }
            }
        }

        Given("람다용 서버 테스트") {
            val ktor = KtorApplicationUtil.buildClient {
                configureRouting()
            }


            When("루트 입력시") {
                Then("리다이렉트 설정된 경로 호출") {
                    ktor.get("/").apply {
                        status shouldBe HttpStatusCode.OK
                        val text = bodyAsText()
                        text shouldBe contain("메인 데모 화면입니다")
                    }
                }
            }

            Then("path 호출 데모") {
                ktor.get("/demo/user/123").apply {
                    status shouldBe HttpStatusCode.OK
                    val text = bodyAsText()
                    text shouldBe contain("userId=[123]")

                    call.response.headers.entries().forEach { e ->
                        log.info { " header -> ${e.key} : ${e.value}" }
                    }
                }
            }
        }
    }

}