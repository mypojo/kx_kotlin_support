package net.kotlinx.html

import kotlinx.html.*


/** Any를 구분하기위한 인터페이스 */
interface HTMLTagAction {
    fun invoke(td: Tag)
}

/** 인라인 HTMl 생성기 */
inline fun makeHtml(crossinline action: (Tag) -> Unit): HTMLTagAction {
    return object : HTMLTagAction {
        override fun invoke(blockTag: Tag) {
            action(blockTag)
        }
    }
}

/** Tag 에서도 쓸 수 있는 간단 라인 개행 */
fun Tag.newLine(able: Boolean) {
    if (!able) return

    if (this is HtmlBlockTag) {
        br()
    }
}

/** 라인을 분리해서 텍스트 입력해준다. */
fun Tag.writeLines(data: Any) {

    when (data) {
        is List<*> -> {
            data.forEachIndexed { index, any ->
                newLine(index != 0)
                when (any) {
                    is HTMLTagAction -> any.invoke(this)
                    else -> text(any.toString())
                }
            }
        }

        is HTMLTagAction -> data.invoke(this)
        else -> text(data.toString())
    }


}

/** HTML 태그 그대로 보여줌. 코드 확인용 */
fun HTMLTag.html(html: String) {
    this.unsafe { +html }
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