package net.kotlinx.playwright


import com.microsoft.playwright.Locator
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.file.slash
import net.kotlinx.number.toSiText
import net.kotlinx.time.TimeFormat
import java.io.File


/**
 * 스크린샷 찍기
 * */
class PlaywrightScreenshot : (PlaywrightInput) -> File {

    @Kdsl
    constructor(block: PlaywrightScreenshot.() -> Unit = {}) {
        apply(block)
    }

    /** 작업 홈 디렉토리 */
    lateinit var homeDir: File

    /** 네이밍 룰 */
    var fileNaming: (name: String) -> String = { name -> "${name}_${TimeFormat.YMDHM_F02.get()}.png" }

    /** 캡쳐 선택자 */
    var cssSelector: String = "body"

    override fun invoke(input: PlaywrightInput): File {
        val page = input.page
        val element = page.locator(cssSelector)
        val file = homeDir.slash(fileNaming(input.name))
        element.screenshot(Locator.ScreenshotOptions().setPath(file.toPath()))
        log.debug { " -> [${input.name}] screenshot (${file.length().toSiText()} -> ${file.absolutePath}" }
        return file
    }


    companion object {

        private val log = KotlinLogging.logger {}


    }

}
