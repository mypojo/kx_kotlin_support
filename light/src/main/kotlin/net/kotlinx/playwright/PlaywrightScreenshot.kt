package net.kotlinx.playwright


import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.ScreenshotType
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.file.slash
import net.kotlinx.number.toSiText
import net.kotlinx.time.TimeFormat
import java.io.File


class PlaywrightScreenshot : (PlaywrightInput) -> File {

    @Kdsl
    constructor(block: PlaywrightScreenshot.() -> Unit = {}) {
        apply(block)
    }

    /** 작업 홈 디렉토리 */
    lateinit var homeDir: File

    /** 네이밍 룰 */
    var fileNaming: (input: PlaywrightInput) -> File = { homeDir.slash(it.group).slash("${it.name}_${TimeFormat.YMDHM_F02.get()}.jpeg") }

    /**
     * 캡쳐 선택자
     * 보통 css 셀렉터로 구하면됨
     * */
    var select: (Page) -> Locator = { it.locator("body") }

    /**
     * 커스텀 설정시 강제 세팅
     * 이거 설정해야지 스샷이 안짤림. 이거 안하면  height 이 720로 설정되는듯
     * 상품의 경우 넉넉히 잡아야함
     *  */
    var viewportHeight: Int? = null

    override fun invoke(input: PlaywrightInput): File {
        val page = input.page
        val element = select(page)
        log.trace { " => ${element.boundingBox().width} / ${element.boundingBox().height}" }
        viewportHeight?.let {
            page.setViewportSize(element.boundingBox().width.toInt(), it)
        }

        val file = fileNaming(input)
        element.screenshot(
            Locator.ScreenshotOptions()
                .setPath(file.toPath())
                .setType(ScreenshotType.JPEG)
                .setOmitBackground(true)
        )
        log.info { " -> [${input.name}] screenshot (${file.length().toSiText()} -> ${file.absolutePath}" }
        return file
    }

    companion object {

        private val log = KotlinLogging.logger {}


    }

}
