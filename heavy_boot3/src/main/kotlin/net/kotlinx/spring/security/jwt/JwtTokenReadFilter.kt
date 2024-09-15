package net.kotlinx.spring.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import net.kotlinx.collection.findFirstNotnull
import net.kotlinx.spring.security.SimpleAuthenticationToken
import net.kotlinx.spring.servlet.cookieMap
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.GenericFilterBean

class JwtTokenReadFilter(
    /** 로그인 없을때 사용한 디폴트(익명) 사용자 */
    private val defaultUser: UserDetails,
    /** null이면 jwt 인증이 불가능한것으로 간주한다. */
    private val tokenProvider: (String?) -> UserDetails?,
) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        val servletRequest = request as HttpServletRequest
        val auth = listOf(
            { servletRequest.getHeader(AUTHORIZATION_HEADER) },
            { servletRequest.cookieMap[AUTHORIZATION_HEADER] },
        ).findFirstNotnull()

        //무조건 호출은 한번 함 (디버깅 이슈)
        when (val user = tokenProvider(auth)) {
            null -> {
                log.trace { " -> 인증정보 없음" }
                SecurityContextHolder.getContext().authentication = SimpleAuthenticationToken(defaultUser)
            }

            else -> {
                log.trace { " -> 인증정보 context에 저장 : $user" }
                SecurityContextHolder.getContext().authentication = SimpleAuthenticationToken(user)
            }
        }
        chain.doFilter(request, response)
    }

    companion object {
        private val log = KotlinLogging.logger {}

        const val AUTHORIZATION_HEADER = "Authorization"
    }
}