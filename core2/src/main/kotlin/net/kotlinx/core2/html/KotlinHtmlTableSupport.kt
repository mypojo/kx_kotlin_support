package net.kotlinx.core2.html

import kotlinx.html.*

/**
 * 텍스트 그리드로 변환
 * ex) table { write(fundingHeader,fundingDatas) }
 *  */
fun TABLE.write(headers: List<String>, datas: List<Array<*>>, nullString: String = "") {
    thead {
        tr {
            headers.forEach { th { +it } }
        }
    }
    tbody {
        datas.forEach { line ->
            tr {
                line.forEach { data ->
                    td {
                        insert(data, nullString)
                    }
                }
            }
        }
    }
}

private fun TD.insert(data: Any?, nullString: String) {
    when (data) {
        is List<*> -> {
            data.forEach { each ->
                insert(each, nullString)
                br
            }
        }

        is HtmlLink -> {
            a {
                href = data.href
                target = "_blank" //새창열기
                +data.name //값이 제일 뒤에 와야한다.
            }
        }

        is HtmlStyle -> {
            //개별 적용을 위해 span태그 사용
            span {
                style = data.style
                +data.value //값이 제일 뒤에 와야한다.
            }
        }

        else -> +(data?.toString() ?: nullString)
    }
}