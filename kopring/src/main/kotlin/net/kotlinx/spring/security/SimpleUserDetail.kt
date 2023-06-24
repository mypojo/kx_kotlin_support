package net.kotlinx.spring.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * UserDetails 간단 구현 버전
 * */
open abstract class SimpleUserDetail(
    open var loginId: String = "unknown",
    open var auths: Collection<out GrantedAuthority> = emptyList(),
) : UserDetails {

    /** 로그인 시에만 임시로 사용한다. */
    var pwd: String = ""

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = auths.toMutableList()

    override fun getPassword(): String = pwd

    override fun getUsername(): String = loginId

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

}