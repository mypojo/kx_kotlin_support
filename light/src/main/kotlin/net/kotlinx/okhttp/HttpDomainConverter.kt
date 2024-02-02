package net.kotlinx.okhttp

import net.kotlinx.core.Kdsl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.IDN

/**
 * 참고사항!!
 * 도메인은 대소문자를 구분하지 않고 동일한것으로 간주함! -> 여기서는 무조건 소문자 리턴
 * 하지만 그 뒤의 path는 대소문자를 구분함
 * https://datatracker.ietf.org/doc/html/rfc1034#section-3.5
 * */
class HttpDomainConverter {

    @Kdsl
    constructor(block: HttpDomainConverter.() -> Unit = {}) {
        apply(block)
    }

    /**
     * path를 레벨단위로 잘라준다
     * */
    var pathLevel: Int = 0


    /**
     * 한글 도메인의 경우 http://xn--2s2bi8mdf.kr/ 와 같이 퓨니코드 도메인으로 변환됨
     * 이걸 다시 한글로 변환 해줄지 여부
     * */
    var toKr: Boolean = true

    /**
     * 도메메인에서 프로토콜 제거 & 쿼리스트링 제거 & path를 레벨단위로 잘라준다.
     * 마지막 "/" (trailing slash)를 제거한다. -> 도메인처럼 보이게 고려함
     * ex) 네이버의 비즈센터 URL을 정규화 시킴
     *  */
    fun normalize(url: String): String {
        val httpUrl = url.toHttpUrl()

        val host = run {
            val host1 = httpUrl.host
            val host2 = if (toKr) fromPunycode(host1) else host1
            host2
        }

        val append = httpUrl.pathSegments.take(pathLevel).joinToString("/")
        if (append.isEmpty()) return host

        return "${host}/${append}"
    }


    companion object {

        /** 한글을 퓨니코드로 변환 */
        fun toPunycode(text: String): String = IDN.toASCII(text)

        /** 퓨니코드를 한글로 변환 */
        fun fromPunycode(text: String): String = IDN.toUnicode(text)

    }


}
