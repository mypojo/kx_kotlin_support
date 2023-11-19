package net.kotlinx.core.html

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.kotlinx.core.Poo
import net.kotlinx.core.htmx.htmxButtonGet
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.io.File

class KotlinHtmlSupportKtTest : TestRoot() {

    @Test
    fun test() {

        val headers = listOf("이름", "소속", "나이", "실행")

        val datas = listOf(
            Poo("개똥이", "삼성", 12),
            Poo("영감님", "토요다 자동차", 34),
        )

        val html = createHTML().html {
            setDefault("데모 테스트") {
                script {
                    src = "https://unpkg.com/htmx.org@1.8.6"
                }
            }
            body {
                h3 { +"제공기능" }
                div {
                    table {
                        thead { tr { headers.forEach { th { +it } } } }
                        tbody {
                            datas.forEach { d ->
                                tr {
                                    td {
                                        +d.name
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
        }
        val file = File(ResourceHolder.getWorkspace(), "kotlin.html")
        file.writeText(html)
        log.warn { " -> ${file.absolutePath}" }
    }
}
