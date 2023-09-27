package net.kotlinx.core.html

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

@Suppress("SameParameterValue") //뭔지 모르겠다..
private fun TD.insert(data: Any?, nullString: String) {
    when (data) {
        is List<*> -> {
            data.forEachIndexed { index, each ->
                insert(each, nullString)
                //마지막에는 라인분리 하지 않음
                if(index != data.lastIndex){
                    br
                }
            }
        }

        is HtmlData -> {
            data.insertHtml(this)
        }

        else -> +(data?.toString() ?: nullString)
    }
}