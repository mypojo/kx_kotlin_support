package net.kotlinx.okhttp

/**
 * 구현체와 상관없이 HTTP 요청을 래핑하는 객체
 * 공통화된 로깅에 사용됨
 * */
data class HttpReq(
    val url:String,
)