package net.kotlinx.html

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
            attributes["class"] = "container-fluid"  //picocss 조금 달라짐
            block()
        }
    }
}


/** 프로젝트마다 오버라이드 해서 사용할것! */
fun HTML.setDefault(title: String, block: HEAD.() -> Unit = {}) {
    lang = "ko"
    attributes["data-theme"] = "dark"
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

        /**
         * 가벼운 CSS  프레임워크
         * https://picocss.com/docs/table 참고
         * */
        link {
            rel = "stylesheet"
            href = "https://cdn.jsdelivr.net/npm/@picocss/pico@1/css/pico.min.css"
        }
        style {
            unsafe {
//                raw(
//                    """
//                        :root {
//                            font-size: 14px;
//                        }
//                    """.trimIndent()
//                )
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