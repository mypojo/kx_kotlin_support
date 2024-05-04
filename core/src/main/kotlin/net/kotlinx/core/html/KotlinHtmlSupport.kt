package net.kotlinx.core.html

import kotlinx.html.*
import kotlinx.html.stream.createHTML

/**
 * 간단 생성버전.
 * body 부터 시작함
 * 확장 고려 X
 * */
fun kotlinHtml(title: String, block: BODY.() -> Unit): String {
    return "<!doctype html>" + createHTML().html {
        setDefault(title) {
            script {
                src = "https://unpkg.com/htmx.org@1.8.6"
            }
        }
        body {
            block()
        }
    }
}


/** 프로젝트마다 오버라이드 해서 사용할것! */
@Deprecated("이상해!!")
fun HTML.setDefault(title: String, block: HEAD.() -> Unit = {}) {
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
            unsafe {
                +DEFAULT_TABLE.trimIndent()
            }
        }
        block()
    }
}

/**
 * 공백문자(nbsp) 삽입.
 * unsafe 가 없는경우 태그가 아니라 문자로 입력됨
 *  */
fun HTMLTag.space() {
    unsafe {
        +Entities.nbsp.text
    }
}

/**
 * 간단 사용
 * padding -> 16 -> 10
 * 하단에 htmx-indicator 추가
 * */
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
    padding:10px;
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

.htmx-indicator{
    opacity:0;
    transition: opacity 500ms ease-in;
}
.htmx-request .htmx-indicator{
    opacity:1
}
.htmx-request.htmx-indicator{
    opacity:1
}
"""