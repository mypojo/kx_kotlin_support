package net.kotlinx.korean


import com.google.common.collect.Lists
import net.kotlinx.core.file.slash
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.excel.Excel
import net.kotlinx.jsoup.JsoupUtil
import net.kotlinx.komoran.KomoClient
import net.kotlinx.komoran.KomoResult
import net.kotlinx.okhttp.fetch
import net.kotlinx.test.TestLight
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.koin.core.component.inject

/**
 * https://esbook.kimjmin.net/06-text-analysis/6.7-stemming/6.7.2-nori
 * */
class 형태소분석_크롤링 : TestLight() {

    /** 기본 클라이언트 */
    val client: OkHttpClient by inject()

    val komoran = KomoClient()

    val headers = listOf("단어", "타입", "cnt")

    @Test
    fun test() {

        val urls = listOf(
            "https://esbook.kimjmin.net/06-text-analysis/6.7-stemming/6.7.2-nori",
        )

        val crwREsults = urls.map { url ->
            val resp = client.fetch {
                this.url = url
            }
            val texts = JsoupUtil.parseToText(resp.respText)
            val tokens = komoran.parse(texts)
            print(tokens)

            //==================================================== 형태소 XX ======================================================
            val tokens2 = texts.asSequence().flatMap { it.split(" ") }
                .filter { it.length >= 2 } //2자리 이상
                .groupBy { it }.map { e -> KomoResult(e.key, "", e.value.size) }.sortedByDescending { it.cnt }
            print(tokens2)
            url to tokens
        }

        val xls = Excel()
        xls.createSheet("키워드_형태소분석").apply {
            addHeader(Lists.newArrayList("도메인", "키워드", "형태소", "카운트"))
            crwREsults.forEach { e ->
                e.second.forEach {
                    writeLine(arrayOf(e.first, it.kwdName, it.pos, it.cnt))
                }
            }
        }
        val out = ResourceHolder.getWorkspace().slash("crw").slash("crw_kwd.xls")
        xls.wrap().write(out)
        log.warn { "결과파일 $out" }


    }

    private fun print(tokens: List<KomoResult>) {
        println(tokens.map { it.kwdName })
        //tokens.map { token -> arrayOf(token.kwdName, token.pos, token.cnt) }.also { headers.toTextGrid(it).print() }
    }

}