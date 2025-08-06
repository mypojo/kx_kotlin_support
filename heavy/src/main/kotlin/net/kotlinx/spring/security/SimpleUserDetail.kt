package net.kotlinx.spring.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * kotlin용 UserDetails 간단 구현 버전
 * */
@Deprecated("JWT 사용시 가능하면 래퍼를 만들어서 사용")
open abstract class SimpleUserDetail(
    open val loginId: String,
    open val auths: Collection<out GrantedAuthority>,
) : UserDetails {

    /** 로그인 시에만 임시로 사용한다. DB에서 로드한 PWD를 여기 담아줌 */
    var pwd: String = ""

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = auths.toMutableList()

    override fun getPassword(): String = pwd

    override fun getUsername(): String = loginId

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

}