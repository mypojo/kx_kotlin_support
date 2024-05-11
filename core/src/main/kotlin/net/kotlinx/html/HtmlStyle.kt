package net.kotlinx.html

/** 간단 색칠에 사용 */
enum class HtmlStyle(val style: String) {
    BLUE("color:blue;"),
    RED("color:red;"),
    ;

    companion object {
        fun style(ok: Boolean): String = if (ok) BLUE.style else RED.style
    }
}