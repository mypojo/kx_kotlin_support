package net.kotlinx.korean


import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import net.kotlinx.excel.Excel
import net.kotlinx.file.slash
import net.kotlinx.jsoup.JsoupUtil
import net.kotlinx.koin.Koins.koin
import net.kotlinx.komoran.KomoClient
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.okhttp.HttpDomainConverter
import net.kotlinx.okhttp.fetch
import net.kotlinx.string.toTextGrid
import net.kotlinx.system.ResourceHolder
import okhttp3.OkHttpClient


class KomoClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("Komoran") {
            val strToAnalyze = "대한민국은 민주공화국이다."

            Then("두가지 모델 비교") {
                DEFAULT_MODEL.values().forEach { model ->
                    val komoran = Komoran(model)
                    val analyzeResultList = komoran.analyze(strToAnalyze)

                    log.info { "모델 : $model" }
                    val datas = analyzeResultList.tokenList.map { token -> arrayOf(token.morph, token.pos) }
                    listOf("단어", "타입").toTextGrid(datas).print()
                }
            }

            val client: OkHttpClient = koin()

            val komoran = KomoClient(DEFAULT_MODEL.LIGHT)

            val converter = HttpDomainConverter {}

            Then("크롤링 후 파싱") {
                val urls = listOf(
                    "https://giftinfo.co.kr/shop/webcart.php?page=1&cate1=226&cate2=1872&cate3=5985&n_media=27758&n_query=%EB%B0%A9%EC%9A%B8%ED%86%A0%EB%A7%88%ED%86%A0&n_rank=1&n_ad_group=grp-a001-01-000000023582240&n_ad=nad-a001-01-000000238434955&n_keyword_id=nkw-a001-01-000005232050837&n_keyword=%EB%B0%A9%EC%9A%B8%ED%86%A0%EB%A7%88%ED%86%A0&n_campaign_type=1&n_ad_group_type=1&n_match=1&NaPm=ct%3Dlukx09aw%7Cci%3D0Aa0001ZjuTAwHsUuKX%5F%7Ctr%3Dsa%7Chk%3D8451d327fbb96c9f82b17d35a45ef6a02fe0cf40",
                    "https://www.jobplanet.co.kr/companies/315629/landing/11%EC%8B%9C11%EB%B6%84",
                    //"https://www.hwgi.kr/report?campaign=keyword&media=google&utm_medium=keyword&utm_source=google_pc&utm_campaign=report&utm_content=231130_googlesa&utm_term=%EB%B3%B4%ED%97%98%EC%A0%95%EB%A6%AC&gad_source=1&gclid=CjwKCAjw_LOwBhBFEiwAmSEQAXxpktfI3qlY9lLOYLIr7esZSB0eYfSo6bi0_d8a4Az2alKcTvNkuhoCvA8QAvD_BwE",
                )

                val crwResults = urls.map { url ->
                    val resp = client.fetch {
                        this.url = url
                    }
                    val texts = JsoupUtil.parseToText(resp.respText)
                    log.debug { "텍스트 ${texts.size}건 -> 샘플 : ${texts.take(10)}" }
                    val tokens = komoran.parse(texts)
                    converter.normalize(url) to tokens
                }

                val xls = Excel()
                xls.createSheet("키워드_형태소분석").apply {
                    addHeader(listOf("도메인", "키워드", "형태소", "카운트"))
                    crwResults.forEach { e ->
                        e.second.forEach {
                            writeLine(arrayOf(e.first, it.kwdName, it.pos, it.cnt))
                        }
                    }
                }
                val out = ResourceHolder.WORKSPACE.slash("crw").slash("crw_kwd.xls")
                xls.wrap().write(out)
                log.warn { "결과파일 $out" }
            }

        }
    }

}