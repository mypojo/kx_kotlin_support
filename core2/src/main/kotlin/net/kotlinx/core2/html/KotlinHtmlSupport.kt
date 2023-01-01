package net.kotlinx.core2.html

import kotlinx.html.*

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