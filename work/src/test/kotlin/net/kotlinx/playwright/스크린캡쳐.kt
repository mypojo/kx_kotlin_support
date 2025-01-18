package net.kotlinx.playwright

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Playwright
import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder

/**
 * https://github.com/Kotlin/dataframe
 * */
class 스크린캡쳐 : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("캡쳐") {

            Then("윈도우 로컬에서 돌리기") {
                val file = ResourceHolder.WORKSPACE.slash("스샷").slash("screenshot.png")
                Playwright.create().use { playwright ->
                    val browser = playwright.chromium().launch()
                    val page = browser.newPage()

                    try {
                        // 특정 URL에 접속
                        page.navigate("https://playwright.dev/docs/intro")

                        // 스크린샷을 찍을 특정 요소 선택 (CSS 선택자 사용)
                        // 예: id가 'content'인 요소의 스크린샷
                        val element = page.locator("body")

                        // 요소 스크린샷 찍기
                        element.screenshot(
                            Locator.ScreenshotOptions()
                                .setPath(file.toPath())
                        )

                        println("스크린샷 저장 완료!")
                    } catch (e: Exception) {
                        println("스크린샷 캡처 중 오류 발생: ${e.message}")
                    } finally {
                        browser.close()
                    }
                }
                println(file)
            }

        }
    }

}