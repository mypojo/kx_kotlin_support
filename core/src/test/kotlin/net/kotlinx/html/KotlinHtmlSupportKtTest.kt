package net.kotlinx.html

import io.kotest.matchers.ints.shouldBeGreaterThan
import kotlinx.html.*
import net.kotlinx.file.slash
import net.kotlinx.html.htmx.htmxButtonGet
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder

class KotlinHtmlSupportKtTest : BeSpecLog() {

    init {

        initTest(KotestUtil.FAST)

        data class Poo(
            val name: String,
            val group: String,
            val age: Int,
        ) {
            var parent: Poo? = null
        }

        Given("kotlinHtml") {
            Then("html 파일 생성 테스트") {

                val headers = listOf("이름", "소속", "나이", "실행")
                val datas = listOf(
                    Poo("개똥이", "삼성", 12),
                    Poo("영감님", "토요다 자동차", 34),
                )

                val html = kotlinHtml("데모 테스트 v2") {
                    h3 { +"제공기능" }
                    div {
                        table {
                            //attributes["data-theme"] = "light"
                            thead {
                                tr { headers.forEach { th { +it } } }
                            }
                            tbody {
                                datas.forEach { d ->
                                    tr {
                                        td {
                                            spanStyle(d.name, true)
                                            br
                                            link("네이버링크", "https://naver.com")
                                        }
                                        td { +d.group }
                                        td { +d.age.toString() }
                                        td {
                                            htmxButtonGet {
                                                btnName = "실행버튼"
                                                dataUrl = "/aa/bb"
                                                targetId = "1234"
                                                indicatorMsg = "실행중.."
                                            }
                                        }
                                    }
                                }
                            }
                            tfoot {
                                tr {
                                    td { +"합계" }
                                    td { +"A" }
                                    td { +"55" }
                                    td { +"-" }
                                }
                            }
                        }
                    }
                }

                html.length shouldBeGreaterThan 10
                val htmlFile = ResourceHolder.getWorkspace().slash("KotlinHtmlSupport").slash("index.html")
                htmlFile.writeText(html)
                log.info { "htmlFile $htmlFile" }
            }
        }

    }
}
