package net.kotlinx.playwright


import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.getOrSet


/**
 * 스크린샷 클라이언트.
 * 무거우니 하나만 사용해야할듯
 * ex) 특정 URL에 접속하여 스크린샷 찍기 -> 정상 페이지인지 LLM으로 확인
 * */
class PlaywrightClient : AutoCloseable {

    @Kdsl
    constructor(block: PlaywrightClient.() -> Unit = {}) {
        apply(block)
    }

    var ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"

    var header = mapOf(
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language" to "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
        "Accept-Encoding" to "gzip, deflate, br",
        "Connection" to "keep-alive",
        "Upgrade-Insecure-Requests" to "1",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "none",
        "Sec-Fetch-User" to "?1",
        //"Referer" to "https://search.naver.com/search.nave/",
    )

    //==================================================== 내부사용 ======================================================

    /** 스래드별 캐싱 */
    private val playwrightContext = ThreadLocal<PlaywrightContext>()

    /** 전체 플레이 */
    private val allPlaywrights = CopyOnWriteArrayList<PlaywrightContext>()

    /** 페이지 로드 실행 */
    fun <T> page(input: PlaywrightInput, block: (input: PlaywrightInput) -> T): T {
        val playwrightContext = playwrightContext.getOrSet {
            val play = Playwright.create()!!
            val browser = play.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(true)
            )!!

            val context = browser.newContext(
                Browser.NewContextOptions()
                    .setTimezoneId("Asia/Seoul")
                    .setLocale("ko-KR")
                    .setUserAgent(ua)
                    .setExtraHTTPHeaders(header)
                //.setProxy() //프록시
            )!!

            log.info { "[${Thread.currentThread().name}] play & browser 생성됨" }
            PlaywrightContext(play, browser, context).also {
                allPlaywrights.add(it)
            }
        }

        return playwrightContext.context.newPage().use { page ->
            page.navigate(input.url)
            page.waitForLoadState(LoadState.NETWORKIDLE) //NETWORKIDLE 로 해야 페이지 정상 로딩된후 캡쳐 가능함
            input.page = page
            block(input)
        }
    }

    override fun close() {
        log.info { "전체 ${allPlaywrights.size}건의 play가 close됩니다.." }
        allPlaywrights.forEach {
            it.browser.close()
            it.play.close()
        }
    }

    /** 개발 편의상, 한개의 클라이언트에 1개의 브라우저만 사용 */
    data class PlaywrightContext(
        val play: Playwright,
        val browser: Browser,
        val context: BrowserContext,
    )

    companion object {

        private val log = KotlinLogging.logger {}

    }

}
