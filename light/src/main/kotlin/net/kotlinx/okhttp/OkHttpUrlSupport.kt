package net.kotlinx.okhttp

import okhttp3.HttpUrl

/** 조합 확인용 */
fun HttpUrl.toUrlString(): String {
    val queryString = if (this.querySize == 0) "" else "?${this.encodedQuery}"
    return "${this.scheme}://${this.host}${this.encodedPath}${queryString}"
}

/**
 * 쿼리스트링을 map으로 만들어준다.
 * 부분 교체 or 데이터 추출용
 *  */
fun HttpUrl.toQueryMap(): MutableMap<String, String?> = this.queryParameterNames.associateWith { this.queryParameter(it) }.toMutableMap()