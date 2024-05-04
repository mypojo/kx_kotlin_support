package net.kotlinx.korean


import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import mu.KotlinLogging
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.kotest.BeSpecLight
import org.junit.jupiter.api.Test


class 형태소분석_단일 : BeSpecLight() {

    private val headers = listOf("단어", "타입")

    val komoran: Komoran = Komoran(DEFAULT_MODEL.FULL)

    val log = KotlinLogging.logger {}

    @Test
    fun test() {

        val strToAnalyze = "대한민국은 민주공화국이다."

        DEFAULT_MODEL.values().forEach { model ->
            val komoran: Komoran = Komoran(model)
            val analyzeResultList = komoran.analyze(strToAnalyze)

            log.info { "모델 : $model" }
            val datas = analyzeResultList.tokenList.map { token -> arrayOf(token.morph, token.pos) }
            headers.toTextGrid(datas).print()
        }


    }

}