package net.kotlinx.core.html

import kotlinx.html.*

/**
 * HTML을 구성해주는 객체
 * DSL 태그 구현체가 한개가 아닌 여러개를 구현하개 되어있다.
 * 여기서는 범용으로 쓸것임으로 다 구현함
 *  */
interface HtmlData {
    /** 일반적으로 테이블을 구성하는 TD 에 사용 */
    fun <T> insertHtml(body: T) where T : HTMLTag, T : HtmlBlockTag
}

/** a 태그 링크가 적용된 값 */
data class HtmlDataSet(val name: String, val href: String) : HtmlData {
    override fun <T : HTMLTag> insertHtml(body: T) where T : HtmlBlockTag {
        body.a {
            href = this@HtmlDataSet.href
            target = "_blank" //새창열기
            +name //값이 제일 뒤에 와야한다.
        }
    }

}

/** HTML 태그 그대로 보여줌 */
data class HtmlUnsafe(val html: String) : HtmlData {
    override fun <T : HTMLTag> insertHtml(body: T) where T : HtmlBlockTag {
        body.unsafe {
            +html //강제주입
        }
    }
}

/** 스타일이 적용된 간단 텍스트 */
data class HtmlStyle(val value: String) : HtmlData {

    override fun <T : HTMLTag> insertHtml(body: T) where T : HtmlBlockTag {
        //개별 적용을 위해 span태그 사용
        body.span {
            style = this@HtmlStyle.style
            +value //값이 제일 뒤에 와야한다.
        }
    }

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