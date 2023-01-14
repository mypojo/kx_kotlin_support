package net.kotlinx.core2.html

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.kotlinx.TestRoot
import org.junit.jupiter.api.Test

class KotlinHtmlTableSupportKtTest : TestRoot() {


    @Test
    fun test() {

        val html = createHTML().html {
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