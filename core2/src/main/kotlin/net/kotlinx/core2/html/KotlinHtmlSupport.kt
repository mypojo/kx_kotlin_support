package net.kotlinx.core2.html

import kotlinx.html.*

/** a 태그 링크가 적용된 값 */
data class HtmlLink(val name: String, val href: String)

/** 스타일이 적용된 값 */
data class HtmlStyle(val value: String) {

    var style: String = ""

    /** 성공 실패 등을 나타낼때 */
    fun ok(ok: Boolean): HtmlStyle {
        style = if (ok) BLUE else RED
        return this
    }

    companion object {
        const val BLUE = "color:blue;"
        const val RED = "color:red;"
    }

}

fun HTML.setDefault(title: String) {
    lang = "ko"
    head {
        meta { charset = "utf-8" }
        meta {
            httpEquiv = "X-UA-Compatible"
            content = "IE=edge"
        }
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1"
        }
        title { +title }
        style {
            +DEFAULT_TABLE.trimIndent()
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