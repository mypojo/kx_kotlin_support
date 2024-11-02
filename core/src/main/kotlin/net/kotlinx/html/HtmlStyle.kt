package net.kotlinx.html

/** 간단 색칠에 사용 */
enum class HtmlStyle(val style: String) {
    BLUE("color:blue;"),
    GREEN("color:green;"),
    RED("color:red;"),
    GRAY("color:gray;"),
    ;

    companion object {
        fun style(ok: Boolean): String = if (ok) GREEN.style else RED.style
    }
}