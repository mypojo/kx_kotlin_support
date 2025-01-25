package net.kotlinx.playwright


import com.microsoft.playwright.Page


data class PlaywrightInput(
    /** ID나 이름 등의 구분자 */
    val name: String,
    val page: Page,
)