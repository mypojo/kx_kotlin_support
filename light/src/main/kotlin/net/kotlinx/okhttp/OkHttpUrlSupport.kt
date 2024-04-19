package net.kotlinx.okhttp

import okhttp3.HttpUrl

/**
 * 간단 URL 생성기 (크롤링 등)
 * 원본 URL에서 쿼리 파라메터 등을 간단하게 추가할때 사용됨
 *  */
fun HttpUrl.build(block: HttpUrl.Builder.() -> Unit = {}): String {
    val builder = this.newBuilder()
    builder.apply(block)
    return builder.build().toString()
}

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