package net.kotlinx.spring.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 기본 인증기의 커스텀 버전
 */
class SimpleUsernamePasswordAuthenticationFilter : UsernamePasswordAuthenticationFilter() {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        //범용 인증토큰을 만들어서 리턴함
        val authentication: UsernamePasswordAuthenticationToken = super.attemptAuthentication(request, response) as UsernamePasswordAuthenticationToken
        return SimpleAuthenticationToken(authentication.principal as UserDetails)
    }
}
