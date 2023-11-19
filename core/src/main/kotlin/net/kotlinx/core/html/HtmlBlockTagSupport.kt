package net.kotlinx.core.html

import kotlinx.html.HtmlBlockTag
import kotlinx.html.a
import kotlinx.html.span
import kotlinx.html.style


/** a 태그 링크가 적용된 값 */
fun HtmlBlockTag.link(name: String, href: String) {
    this.a {
        this.href = href
        target = "_blank" //새창열기
        +name //값이 제일 뒤에 와야한다.
    }
}

/** 간단 색칠  */
fun HtmlBlockTag.spanStyle(value: String, style: String) {
    //개별 적용을 위해 span태그 사용
    this.span {
        this.style = style
        +value //값이 제일 뒤에 와야한다.
    }
}

/** 간단 색칠  */
fun HtmlBlockTag.spanStyle(value: String, ok: Boolean) = this.spanStyle(value, HtmlStyle.style(ok) )