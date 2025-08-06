package net.kotlinx.spring.security

import org.springframework.security.authentication.AccountStatusException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetails

/**
 * AbstractUserDetailsAuthenticationProvider의 경우 인증이 실패한다면 반드시 예외를 던져야 한다.
 * => 나는 인증 실패시 원래의 인증로직을 타게 하고싶어서 AbstractUserDetailsAuthenticationProvider 를 상속하지않고 하나 더 만들었다.
 *
 * 원래 프로바이더 앞에 와야 한다.  비번 정합 여부와 상관없이 강제 로그인을 하기 위한 로직이다.
 * 경고! 야매성 로직임으로 IP등, 반드시 강력한 수단을 통해서 인증해야 한다.
 *
 *
 */
abstract class AbstractAdminProvider : AuthenticationProvider {

    /**
     * 예외를 잡아서 null을 리턴한다. (즉 다음 프로바이더로 작업을 넘긴다.)
     */
    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        return try {
            //ID와 패스워드를 매칭
            if (authentication.credentials == null) throw BadCredentialsException("is not required login") //이상한 세팅이 있어서 추가
            val password: String = authentication.credentials.toString()
            if (password.isEmpty()) throw BadCredentialsException("pass is required")

            val detail: WebAuthenticationDetails = authentication.details as WebAuthenticationDetails


            val loginId: String = authentication.name
            val ip: String = detail.remoteAddress

            val user = checkAndFind(loginId, password, ip)
            UsernamePasswordAuthenticationToken(user, authentication.credentials, user.authorities).apply {
                details = authentication.details
            }
        } catch (e: AccountStatusException) {
            //계정 상태 예외는 걍 던진다.
            throw e
        } catch (e: AuthenticationException) {
            //일반 인증 예외는 무시한다.
            null
        }
    }

    protected abstract fun checkAndFind(loginId: String, password: String, ip: String): UserDetails

    override fun supports(authentication: Class<*>?): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
