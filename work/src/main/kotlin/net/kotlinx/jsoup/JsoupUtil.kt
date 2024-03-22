package net.kotlinx.jsoup

import net.kotlinx.core.collection.mapNotEmpty
import org.jsoup.Jsoup

object JsoupUtil {

    /** 각 HTML의 element 만 모아서 리턴해줌 */
    fun parseToText(respText: String): Collection<String> {
        val doc = Jsoup.parse(respText)
        // body 태그 선택
        val body = doc.body()
        // 스크립트 태그 제거
        body.select("script").remove()
        // 스타일 태그 제거
        body.select("style").remove()

        val allElements = body.allElements.toList()

        return allElements.mapNotEmpty { it.text() }
    }


}