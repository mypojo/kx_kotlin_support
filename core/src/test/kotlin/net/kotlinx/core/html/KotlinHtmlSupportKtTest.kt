package net.kotlinx.core.html

import kotlinx.html.*
import net.kotlinx.core.Poo
import net.kotlinx.core.file.slash
import net.kotlinx.core.htmx.htmxButtonGet
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class KotlinHtmlSupportKtTest : TestRoot() {

    @Test
    fun test() {

        val headers = listOf("이름", "소속", "나이", "실행")

        val datas = listOf(
            Poo("개똥이", "삼성", 12),
            Poo("영감님", "토요다 자동차", 34),
        )

        val html = kotlinHtml("데모 테스트 v2") {
            h3 { +"제공기능" }
            div {
                table {
                    thead { tr { headers.forEach { th { +it } } } }
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
                }
            }
        }

        val file = ResourceHolder.getWorkspace().slash("work").slash("kotlin.html")
        file.writeText(html)
        log.warn { " -> ${file.absolutePath}" }
    }
}
