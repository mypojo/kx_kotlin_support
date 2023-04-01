package net.kotlinx.module1.slack

import kotlin.text.RegexOption.MULTILINE

/**
 * 슬랙 포맷용 유틸
 */
object SlackFormatUtil {

    /** 한칸 스페이스 포함 */
    val linkPattern = "\\s(https?)://([^:/\\s]+)(:([^/]*))?((/[^\\s/]+)*)?/?([^#\\s?]*)(\\?([^#\\s]*))?(#(\\w*))?\$".toRegex(MULTILINE)

    /** 텍스트 내용에 링크가 있을경우 슬랙 링크로 변환해준다.  */
    fun toSlackLink(text: String, name: String = "링크"): String = text.replace(linkPattern) { " " + name.slackLink(it.value.trim()) }
}