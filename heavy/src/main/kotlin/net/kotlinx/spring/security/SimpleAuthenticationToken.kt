package net.kotlinx.spring.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * principal 하나로 사용하기 위해서 위임함
 * principal = UserTokenData = DDB 저장되는 세션
 * credentials = 사용안함
 * authorities = 디폴트 권한. 사용안함
 *
 * 내장 메모리 객체에 Authentication 를 직접 구현하지 말고 분리할것.
 */
class SimpleAuthenticationToken(
    private val principal: UserDetails
) : Authentication {

    /** 객체의 auth를 무조건 사용한다.  */
    override fun getAuthorities(): Collection<GrantedAuthority> = principal.authorities as Collection<GrantedAuthority>

    override fun getPrincipal(): Any = principal

    override fun isAuthenticated(): Boolean = true

    override fun setAuthenticated(isAuthenticated: Boolean): Unit = throw UnsupportedOperationException("xxxx")

    override fun getName(): String = principal.username

    //==================================================== 사용 안함 ======================================================
    override fun getCredentials(): Any = throw UnsupportedOperationException("xxxx")

    override fun getDetails(): Any = throw UnsupportedOperationException("xxxx")

    companion object {
        //==================================================== static ======================================================
        /**
         * 컨텍스트의 레퍼런스만 리셋한다. 이렇게 해야 확정적으로 더티체킹이 되는 경우가 있음
         * JWT는 이게 필요없음
         * */
        fun authReset() {
            val existAuth = SecurityContextHolder.getContext().authentication as SimpleAuthenticationToken
            val newAuth = SimpleAuthenticationToken(existAuth.getPrincipal() as UserDetails)
            SecurityContextHolder.getContext().authentication = newAuth
        }
    }
}