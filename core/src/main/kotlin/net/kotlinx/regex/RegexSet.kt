package net.kotlinx.regex

import java.util.regex.Pattern

/**
 * 자주 사용되는 정규식 모음
 * 확장함수가 가능해셔서 enum으로 만들지 않음
 * 문법 참고용으로 사용
 *
 * @see net.kotlinx.regex.StringRegexParseSupport
 * */
object RegexSet {

    //==================================================== 숫자 ======================================================

    /** 숫자  */
    val NUMERIC = "\\d*".toRegex()

    /** 숫자 (소수점. 추가)   */
    var NUMERIC_DOT = "[\\d.]*".toRegex()

    /** 빈 공백문자. 로그 split 등에 사용됨 */
    val SPACE = "\\s+".toRegex()

    //==================================================== 변환 ======================================================
    /** 카멜 케이스 */
    val CAMEL = "(?<=[a-zA-Z])[A-Z]".toRegex()

    /**  스네이크 케이스 */
    val SNAKE = "_[a-zA-Z]".toRegex()

    /** 알바벳+숫자 */
    object ALPAH_NUMERIC {

        /** 알파벳+영문+완성형한글  */
        val HAN = "[a-zA-Z\\d가-힣]*".toRegex()

        /** 알파벳+영문+완성형&비완성형한글  */
        val HAN2 = "[a-zA-Z\\d가-힣ㄱ-ㅎㅏ-ㅣ]*".toRegex()

        /** 알파벳+영문+완성형한글+키보드특문  */
        val HAN_PUNCT = "[a-zA-Z\\d가-힣\\p{Punct}\\s]*".toRegex()

    }

    //==================================================== 비지니스 ======================================================

    /** 업무용 정규식  */
    object BUSINESS {
        /** 핸드폰(-없음!)  */
        val HP1 = "^01(?:0|[6-9])(?:\\d{7}|\\d{8})\$".toRegex()

        /** 핸드폰(-)  */
        val HP2 = "^01(?:0|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}\$".toRegex()

        /** url중 http://체크  */
        val URL_HTTP = "^(https?)://([^:/\\s]+)(:([^/]*))?((/[^\\s/]+)*)?/?([^#\\s?]*)(\\?([^#\\s]*))?(#(\\w*))?\$".toRegex()

        /** IP 형식 체크  */
        val IP = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])\$".toRegex()

        /**
         * url중 스키마 제거시
         * ex) domain.removeFrom(URL_SCHEMA)
         *  */
        val URL_SCHEMA = "^(https?)://([^:/\\s]+)(:([^/]*))?((/[^\\s/]+)*)?/?([^#\\s?]*)(\\?([^#\\s]*))?(#(\\w*))?\$".toRegex()

    }

    //==================================================== 함수 ======================================================

    private val regexEscaper = Pattern.compile("[.\\\\+*?\\[^\\]$(){}=!<>|:\\-]")!!

    /** regex 예약어를 치환해준다. */
    fun escape(input: String): String {
        var escaped = input
        val matcher = regexEscaper.matcher(escaped)
        while (matcher.find()) {
            escaped = matcher.replaceAll("\\\\$0")
        }
        return escaped
    }

    /** 두 패턴 사이의 값을 찾는 정규식. (매칭 미포함) */
    fun extract(pref: String, suff: String): String = "(?<=${escape(pref)}).*?(?=${escape(suff)})"

    /** 두 패턴 사이의 값을 찾는 정규식. (매칭 포함)  */
    fun find(pref: String, suff: String): String = "${escape(pref)}.*?${escape(suff)}"

    /** 크롤링용 기본옵션
     * 멀티라인 & 대소문자 구분x
     * */
    val CRW: Set<RegexOption> = setOf(
        RegexOption.MULTILINE,
        RegexOption.DOT_MATCHES_ALL,
        RegexOption.IGNORE_CASE,
    )

}