package net.kotlinx.spring.servlet

import net.kotlinx.collection.toPair
import net.kotlinx.string.decodeBase64

/** 자주 사용되는 인증 파싱 모음 */
object HttpAuthUtil {


    /** ID / 비번 인경우 */
    fun idpass(authorization: String?): Pair<String, String>? {

        if (authorization == null) return null

        return when {
            /** 일반적인 ID / 비번 로그인 */
            authorization.startsWith("Basic ") -> {
                authorization.removePrefix("Basic ").decodeBase64().split(":").toPair()
            }

            else -> throw IllegalArgumentException("Unknown authorization type: $authorization")
        }
    }


}
