package net.kotlinx.komoran

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran

/**
 * https://esbook.kimjmin.net/06-text-analysis/6.7-stemming/6.7.2-nori
 * */
class KomoClient(val model: DEFAULT_MODEL = DEFAULT_MODEL.LIGHT) {

    private val komo: Komoran = Komoran(model)

    /**
     * 무시할 타입들
     * http://kkma.snu.ac.kr/documents/?doc=postag
     *  */
    val ignore = setOf(
        "SS", "SF", "SN", "SW", "SP",
        "JKS", "JKG", "JKO", "JC", "JKB", "JX",
        "VV", "VX", "VCP",
        "MAG",
        "EC", "ETM", "EF", "ETN",
        "XSV",
    )

    fun parse(lines: Collection<String>): List<KomoResult> {
        return lines.asSequence().flatMap { komo.analyze(it).tokenList }
            .filter { it.pos !in ignore }
            .filter { it.morph.length >= 2 } //2자리 이상
            .map { it.morph to it.pos }.groupBy { it }.map { e -> KomoResult(e.key.first, e.key.second, e.value.size) }.toList().sortedByDescending { it.cnt }
    }


}