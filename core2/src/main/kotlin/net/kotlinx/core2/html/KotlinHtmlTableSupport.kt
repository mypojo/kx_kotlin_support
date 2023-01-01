package net.kotlinx.core2.html

import kotlinx.html.*

data class HtmlLink(val name: String, val href: String)

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
                        when (data) {
                            is List<*> -> {
                                data.forEach {
                                    +(it?.toString() ?: nullString)
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

                            else -> +(data?.toString() ?: nullString)
                        }
                    }
                }
            }
        }
    }
}

/** 간단 사용 */
const val DEFAULT_TABLE = """
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;700&display=swap');
body {
    font-family:'Noto Sans KR', sans-serif;
    letter-spacing:-0.02em;
}
h2,
h3 {
    color:#202428;
    text-align:center;
}
h2 {
    margin-top:64px;
    font-weight:700;
    font-size:24px;
}
h3 {
    padding:28px 0;
    font-weight:400;
    font-size:18px;
}
table {
    margin:0 auto 40px;
    border-top:1px solid #e6eaed;
    border-left:1px solid #e6eaed;
    border-collapse:collapse;
    border-radius:20px;
}
table th,
table td {
    padding:16px;
    border-right:1px solid #e6eaed;
    border-bottom:1px solid #e6eaed;
}
table th {
    font-size:11px;
    background:#fcfcfc;
    color:#495057;
}
table td {
    font-size:11px;
    color:#868e96;
}            
"""