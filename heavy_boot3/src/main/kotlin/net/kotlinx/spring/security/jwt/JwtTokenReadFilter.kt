package net.kotlinx.spring.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import net.kotlinx.spring.security.SimpleAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.GenericFilterBean

class JwtTokenReadFilter(
    /** null이면 jwt 인증이 불가능한것으로 간주한다. */
    private val tokenProvider: (String?) -> UserDetails?
) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val auth = (request as HttpServletRequest).getHeader(AUTHORIZATION_HEADER)
        tokenProvider(auth)?.let {
            SecurityContextHolder.getContext().authentication = SimpleAuthenticationToken(it)
            log.trace { " -> 인증정보 context에 저장 : $it" }
        } ?: run {
            log.trace { " -> 인증정보 없음" }
        }
        chain.doFilter(request, response)
    }

    companion object {
        private val log = KotlinLogging.logger {}
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}