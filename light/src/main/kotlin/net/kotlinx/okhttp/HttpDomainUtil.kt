package net.kotlinx.okhttp

import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 참고사항!!
 * 도메인은 대소문자를 구분하지 않고 동일한것으로 간주함!
 * 하지만 그 뒤의 path는 대소문자를 구분함
 * https://datatracker.ietf.org/doc/html/rfc1034#section-3.5
 * */
@Deprecated("HttpDomainConverter 쓰세요")
object HttpDomainUtil {

    /**
     * 도메메인에서 프로토콜 제거 & 쿼리스트링 제거 & path를 레벨단위로 잘라준다. & 도메인 소문자화
     * 마지막 "/" (trailing slash)를 제거한다. -> 도메인처럼 보이게 고려함
     * ex) 네이버의 비즈센터 URL을 정규화 시킴
     * 주의!! 한글 도메인의 경우 http://xn--2s2bi8mdf.kr/ 와 같이 퓨니코드 도메인으로 변환됨
     *  */
    fun normalize(url: String, pathLevel: Int = 0, toKr: Boolean = true): String {
        val httpUrl = url.toHttpUrl()

        val append = httpUrl.pathSegments.take(pathLevel).joinToString("/")
        if (append.isEmpty()) return httpUrl.host

        return "${httpUrl.host.lowercase()}/${append}"
    }


}
