package net.kotlinx.html

import kotlinx.html.*
import net.kotlinx.core.Kdsl

/**
 * 간단 HTML 테이블 구현
 *  */
class HtmlTable {

    @Kdsl
    constructor(block: HtmlTable.() -> Unit = {}) {
        apply(block)
    }

    /** 헤더 */
    lateinit var headers: List<Any>

    /** 데이터 */
    lateinit var dataLines: List<List<Any>>

    /** 헤더 스타일 */
    var headerStyle: Map<String, String> = emptyMap()

    /** 데이터 스타일 */
    var dataStyle: Map<String, String> = mapOf(
        "font-size" to "14px"
    )

    /** 테이블 간단생성 */
    fun HtmlBlockTag.createTable() {
        table {
            thead {
                tr {
                    headers.forEach { head ->
                        th {
                            style = headerStyle.entries.joinToString(";") { "${it.key}:${it.value}" }
                            writeLines(head)
                        }
                    }
                }
            }
            tbody {
                dataLines.forEach { row ->
                    tr {
                        row.forEach {
                            td {
                                style = dataStyle.entries.joinToString(";") { "${it.key}:${it.value}" }
                                writeLines(it)
                            }
                        }
                    }
                }
            }
        }
    }


}