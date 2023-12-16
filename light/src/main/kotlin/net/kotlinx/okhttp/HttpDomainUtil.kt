package net.kotlinx.okhttp

import net.kotlinx.core.string.removeFrom

object HttpDomainUtil {

    /** 프로토롤 제거용 */
    private val PROTO = "^https?://".toRegex()

    /** 도메메인에서 프로토콜 제거 & 접미어 / 제거해준다 */
    fun normalize(domain: String): String = domain.removeFrom(PROTO).substringBefore("/")


}
