package net.kotlinx.ktor.server

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.kotest.modules.ktor.KtorMember
import net.kotlinx.kotest.modules.ktor.KtorMemberConverter
import net.kotlinx.kotest.modules.ktor.allModules

class KtorApplicationUtilTest : BeSpecLight() {

    private val ktor by koinLazy<HttpClient>()
    private val memberConverter by koinLazy<KtorMemberConverter>()

    init {
        initTest(KotestUtil.FAST)

        Given("기본 테스트") {
            Then("간단 실행") {
                testApplication {
                    application {
                        allModules()
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

        Given("시큐리티 테스트") {

            When("로그인필용없음") {
                Then("미인증 -> 200") {
                    val resp = ktor.get("/protected/a")
                    resp.status shouldBe HttpStatusCode.OK
                }
            }

            When("인증필요(JWT=디폴트)") {

                val member = KtorMember("sin", "admin")
                Then("미인증 -> Unauthorized") {
                    val resp = ktor.get("/protected/b")
                    resp.status shouldBe HttpStatusCode.Unauthorized
                }

                Then("인증 -> 200") {

                    val authJwt: HttpRequestBuilder.() -> Unit = {
                        headers {
                            val token = memberConverter.convertTo(member)
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }
                    val resp = ktor.get("/protected/b", authJwt)
                    resp.status shouldBe HttpStatusCode.OK
                    resp.bodyAsText() shouldBe contain(member.name)
                }

                Then("인증 but IP 차단 -> Forbidden") {

                    val authJwt: HttpRequestBuilder.() -> Unit = {
                        headers {
                            val token = memberConverter.convertTo(member)
                            append(HttpHeaders.Authorization, "Bearer $token")
                            append("x-forwarded-for","111.222.333.444")
                        }
                    }
                    val resp = ktor.get("/protected/b", authJwt)
                    resp.status shouldBe HttpStatusCode.Forbidden
                }

            }

            When("인증필요(bearer 커스텀)") {

                val authBearer: HttpRequestBuilder.() -> Unit = {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer abcx")
                    }
                }

                Then("인증 -> 200 => 다른 역할 리턴") {
                    val resp = ktor.get("/protected/c", authBearer)
                    resp.status shouldBe HttpStatusCode.OK
                    resp.bodyAsText() shouldBe contain("ROLE_BEARER")
                }
            }


        }
    }

}