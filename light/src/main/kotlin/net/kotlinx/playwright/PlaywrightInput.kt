package net.kotlinx.playwright


import com.microsoft.playwright.Page
import net.kotlinx.core.Kdsl


class PlaywrightInput {

    @Kdsl
    constructor(block: PlaywrightInput.() -> Unit = {}) {
        apply(block)
    }

    /** 다수의 입력을 그룹화 할때 사용 */
    lateinit var group: String

    /** ID나 이름 등의 구분자 */
    lateinit var name: String

    /** URL */
    lateinit var url: String

    /** 페이지 */
    lateinit var page: Page

}