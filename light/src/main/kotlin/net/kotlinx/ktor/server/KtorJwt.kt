package net.kotlinx.ktor.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import net.kotlinx.core.Kdsl
import net.kotlinx.time.toInstant
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class KtorJwt {

    @Kdsl
    constructor(block: KtorJwt.() -> Unit = {}) {
        apply(block)
    }

    /** 비밀키 */
    lateinit var secretKey: String

    /** 발행자 */
    lateinit var issuer: String

    /** 토큰 대상자 */
    var audiences: List<String> = emptyList()

    /** 알고리즘 */
    private val algorithm by lazy { Algorithm.HMAC512(secretKey) }

    /** 디폴트 expire  */
    var expire: Duration = 7.days

    /** JWT 검중기 */
    val verifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withAudience(*audiences.toTypedArray())
            .withIssuer(issuer)
            .build()
    }

    /**
     * 토큰 파싱.
     * data class 로 생성하는 경우도 있어서 리플렉션 하지 않는다.
     *  */
    fun parseToken(token: String): Map<String, Claim> {
        val decodedJWT = verifier.verify(token)
        return decodedJWT.claims
    }

    /** 토큰 생성 */
    fun createToken(param: Map<String, Any>, expireAfter: Duration = expire): String = JWT.create()
        .withJWTId(UUID.randomUUID().toString()) // 고유 토큰 ID 추가
        .withNotBefore(Date()) // 토큰 활성화 시작 시간 설정
        .withAudience(*audiences.toTypedArray())
        .withIssuer(issuer)
        .withExpiresAt(LocalDateTime.now().plusSeconds(expireAfter.inWholeSeconds).toInstant())
        .apply {
            param.entries.forEach { e ->
                when (val value = e.value) {
                    is String -> withClaim(e.key, value)
                    is Int -> withClaim(e.key, value)
                    is Long -> withClaim(e.key, value)
                    is Boolean -> withClaim(e.key, value)
                    is LocalDateTime -> withClaim(e.key, value.toInstant())
                    else -> throw IllegalArgumentException("지원하지 않는 타입 입니다. $value")
                }
            }
        }
        .sign(algorithm)

}