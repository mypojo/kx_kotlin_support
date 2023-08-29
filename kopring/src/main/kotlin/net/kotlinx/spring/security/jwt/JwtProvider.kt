package net.kotlinx.spring.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import jakarta.validation.ValidationException
import mu.KotlinLogging
import net.kotlinx.core.time.toDate
import org.springframework.security.core.userdetails.UserDetails
import java.security.Key
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration


/**
 * https://jwt.io/
 *  */
class JwtProvider(
    secret: String,
    private val tokenDuration: Duration
) {

    private val log = KotlinLogging.logger {}

    private val secretKey: Key = Base64.getDecoder().decode(secret).let { Keys.hmacShaKeyFor(it) }

    /** 토큰으로 변환 */
    fun <T : UserDetails> createToken(user: T, block: JwtBuilder.() -> Unit): String {
        return Jwts.builder()
            .setSubject(user.username)
            .signWith(secretKey, SignatureAlgorithm.HS512) //암호화 기본
            .setExpiration(LocalDateTime.now().plusMinutes(tokenDuration.inWholeMinutes).toDate())
            .setIssuedAt(Date())
            .apply {
                block(this)
            }
            .compact()
    }

    /**인증 정보 조회  */
    fun parseToken(token: String) = try {
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    } catch (e: SecurityException) {
        log.info { " -> 입력 토큰 $token" }
        throw ValidationException("잘못된 JWT 서명입니다.", e)
    } catch (e: MalformedJwtException) {
        log.info { " -> 입력 토큰 $token" }
        throw ValidationException("잘못된 JWT 서명입니다.", e)
    } catch (e: ExpiredJwtException) {
        log.info { " -> 입력 토큰 $token" }
        throw ValidationException("만료된 JWT 토큰입니다.", e)
    } catch (e: UnsupportedJwtException) {
        log.info { " -> 입력 토큰 $token" }
        throw ValidationException("지원하지 않는 JWT 토큰입니다.", e)
    } catch (e: IllegalArgumentException) {
        log.info { " -> 입력 토큰 $token" }
        throw ValidationException("JWT 토큰이 잘못되었습니다.", e)
    }!!


}