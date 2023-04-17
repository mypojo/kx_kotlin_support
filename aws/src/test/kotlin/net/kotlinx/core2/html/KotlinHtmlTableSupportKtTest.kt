package net.kotlinx.core2.html

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test

class KotlinHtmlTableSupportKtTest : TestRoot() {


    @Test
    fun test() {

        val html = createHTML().html {
            setDefault("test")
            body {
                div {
                    table {

                        write(
                            listOf("ID", "이름"),
                            listOf(
                                arrayOf("111", HtmlLink("aaa", "bbb")),
                                arrayOf(
                                    "111",
                                    listOf(HtmlLink("1", "2"), "bbbb")
                                ),
                                arrayOf(
                                    "111",
                                    listOf(
                                        HtmlStyle("빨간").ok(false),
                                        HtmlStyle("파란").ok(true),
                                    ),
                                ),
                            )
                        )
                    }
                }
                br
            }
        }

        println(html)
    }


}