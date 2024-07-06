package net.kotlinx.html

import kotlinx.html.*


/** 헤더 생성 */
fun TABLE.createHeader(headers: List<Any>) {
    thead {
        tr {
            headers.forEach { head ->
                th {
                    writeLines(head)
                }
            }
        }
    }
}

/** 바디 생성 */
fun TABLE.createBody(dataLines: List<List<Any>>) {
    tbody {
        dataLines.forEach { row ->
            tr {
                row.forEach {
                    td {
                        writeLines(it)
                    }
                }
            }
        }
    }
}

/** 테이블 간단생성 */
fun HtmlBlockTag.createTable(headers: List<Any>, dataLines: List<List<Any>>) {
    table {
        createHeader(headers)
        createBody(dataLines)
    }
}