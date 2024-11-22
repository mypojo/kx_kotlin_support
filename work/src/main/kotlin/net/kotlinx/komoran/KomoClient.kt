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
     * https://happygrammer.github.io/nlp/postag-set/
     *  */
    private val ignore = setOf(
        "SS", "SF", "SN", "SW", "SP",
        "JKS", "JKG", "JKO", "JC", "JKB", "JX",
        /** 용언 (동사, 형용사, 긍정 지정사..) */
        "VV", "VX", "VCP",
        "MAG",
        "EC", "ETM", "EF", "ETN",
        "XSV",
    )

    private val naming = mapOf(
        "NNG" to "일반 명사",
        "NNP" to "고유 명사",
        "NNB" to "의존 명사",
        "NNBC" to "의존 명사 단위 명사",
        "NR" to "수사",
        "NP" to "대명사",
        "VV" to "동사",
        "VA" to "형용사",
        "VX" to "보조 용언",
        "VCP" to "긍정 지정사",
        "VCN" to "부정 지정사",
        "MM" to "관형사",
        "MAG" to "일반 부사",
        "MAJ" to "접속 부사",
        "IC" to "감탄사",
        "JKS" to "주격 조사",
        "JKC" to "보격 조사",
        "JKG" to "관형격 조사",
        "JKO" to "목적격 조사",
        "JKB" to "부사격 조사",
        "JKV" to "호격 조사",
        "JKQ" to "인용격 조사",
        "JX" to "보조사",
        "JC" to "접속 조사",
        "EP" to "선어말 어미",
        "EF" to "종결 어미",
        "EC" to "연결 어미",
        "ETN" to "명사형 전성 어미",
        "ETM" to "관형형 전성 어미",
        "XPN" to "체언 접두사",
        "XSN" to "명사 파생 접미사",
        "XSV" to "동사 파생 접미사",
        "XSA" to "형용사 파생 접미사",
        "XR" to "어근",
        "SF" to "마침표, 물음표, 느낌표",
        "SE" to "줄임표",
        "SS" to "따옴표, 괄호표, 줄표",
        "SSO" to "여는 괄호 (, [",
        "SSC" to "닫는 괄호 ), ]",
        "SP" to "쉼표, 가운뎃점, 콜론, 빗금",
        "SC" to "구분자 , · / :",
        "SO" to "붙임표 (물결, 숨김, 빠짐)",
        "SW" to "기타기호 (논리 수학 기호, 화폐기호)",
        "SY" to "기타",
        "SL" to "외국어",
        "SH" to "한자",
        "SN" to "숫자"
    )

    fun parse(lines: Collection<String>): List<KomoResult> {
        return lines.asSequence().flatMap { komo.analyze(it).tokenList }
            .filter { it.pos !in ignore }
            .filter { it.morph.length >= 2 } //2자리 이상
            .map { it.morph to it.pos }.groupBy { it }.map { e -> KomoResult(e.key.first, e.key.second, naming[e.key.second] ?: "-", e.value.size) }.toList()
            .sortedByDescending { it.cnt }
    }


}