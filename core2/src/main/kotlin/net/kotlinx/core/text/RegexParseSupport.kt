package net.kotlinx.core.text

import net.kotlinx.core.regex.RegexSet

/**
 * 정규표현식을 사용하는 텍스트 파서. 크롤링 등에 사용
 *
 * 아래는 이전에 시도해봤단 실패작들.
 * 1. 텍스트 라인을 사용하는 라인세퍼레이터 파서 -> 싱글라인에 매추 취약함. 복잡한 XML 파싱 힘듬. \n를 활용한 파싱 불가.
 * 2. XML or HTML 파서 -> HTML에 사용시 에러가 너무 많아서 포기.(주석, 잘못된 태그, 특수문법  등등 별게 다 에러를 유발함) 게다가 HTML뿐 아니라 JSON,핸들바,암호화된 문자열 등을 읽을 상황도 많기때문에 사용 불가능
 */
interface RegexParseSupport {

    /**
     * 매칭범위 찾음
     * ex) 작업 전 탐색 범위를 줄이기 위해서 text 잘라냄
     * */
    fun String.find(pair: Pair<String, String>, op: Set<RegexOption> = RegexSet.CRW): String? = RegexSet.find(pair.first, pair.second).toRegex(op).find(this)?.value

    /** 매칭범위 찾음 */
    fun String.findAll(pair: Pair<String, String>, op: Set<RegexOption> = RegexSet.CRW): List<String> =
        RegexSet.find(pair.first, pair.second).toRegex(op).findAll(this).map { it.value }.toList()

    /** 텍스트 값 추출  */
    fun String.extract(pair: Pair<String, String>, op: Set<RegexOption> = RegexSet.CRW): String? = RegexSet.extract(pair.first, pair.second).toRegex(op).find(this)?.value

    /** 텍스트 값 추출 */
    fun String.extractAll(pair: Pair<String, String>, op: Set<RegexOption> = RegexSet.CRW): List<String> =
        RegexSet.extract(pair.first, pair.second).toRegex(op).findAll(this).map { it.value }.toList()

}