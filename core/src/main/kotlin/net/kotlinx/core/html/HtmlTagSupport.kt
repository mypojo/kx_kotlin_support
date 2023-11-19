package net.kotlinx.core.html

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe


/** HTML 태그 그대로 보여줌. 코드 확인용 */
fun HTMLTag.html(html: String) {
    this.unsafe { +html }
}

