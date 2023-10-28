package net.kotlinx.core.html

import kotlinx.html.*


/** HTMX GET 사용하는 버튼 */
class HtmxGetButton(block: HtmxGetButton.() -> Unit = {}) : HtmlData {

    /** 버튼명 */
    lateinit var btnName: String

    /**  데이터 URL ex) "/crw/exe?aa=bb" */
    lateinit var dataUrl: String

    /** 대상 ID */
    lateinit var targetId: String

    /**
     * 교체 모드
     * innerHTML 대상 요소의 내부 HTML을 대체
     * outerHTML 전체 대상 요소를 응답으로 대체
     * */
    var swap: String = "innerHTML"

    /**
     * 데이터 가져올때 여기 인자를 추가로 파라메터로 전송함
     * 형식은 CSS 셀렉터
     * ex) [id='ta-NV_DATALAB_CAT_POP_KWD']
     *  */
    var include: String? = null

    /**
     * 지정하면 "로딩중" 등의 메세지 표시.
     *  */
    var indicatorMsg: String? = null

    /** 커스텀 설정 */
    var btnCustom: BUTTON.() -> Unit = { }

    init {
        block(this)
    }

    override fun <T> insertHtml(body: T) where T : HTMLTag, T : HtmlBlockTag {

        body.div {
            attributes["style"] = "display: flex;" //가로 정렬

            val indicatorId = "indicator_${targetId}"
            button(classes = "btn") {
                attributes["hx-get"] = dataUrl
                attributes["hx-target"] = "#${targetId}" //CSS 선택자
                attributes["hx-swap"] = swap
                include?.let { attributes["hx-include"] = it }
                indicatorMsg?.let { attributes["hx-indicator"] = "#${indicatorId}" } //CSS 선택자
                this.apply(btnCustom) //downstream 이전에 적용해야함
                +btnName
            }

            indicatorMsg?.let {
                space()
                div {
                    id = indicatorId
                    attributes["class"] = "htmx-indicator"
                    +it
                }
            }
        }
    }
}