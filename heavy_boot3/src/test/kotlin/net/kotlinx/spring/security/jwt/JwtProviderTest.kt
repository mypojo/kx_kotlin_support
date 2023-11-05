package net.kotlinx.spring.security.jwt

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import kotlin.time.Duration.Companion.hours


class JwtProviderTest : TestRoot() {


    @Test
    fun test() {

        val provider = JwtProvider("secretkey123123secretkey123123secretkey11secretkey123123secretkey123123secretkey11", 24.hours)

        val user = User("김첨지")
        val token = provider.createToken(user) {
            claim("a", "bb")
        }
        log.info { "token $token" }

        val user2 = provider.parseToken(token)
        check(user2.subject == user.name)
        check(user2["a"] == "bb")

    }

    class User(val name: String) : UserDetails {

        override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
            TODO("Not yet implemented")
        }

        override fun getPassword(): String {
            TODO("Not yet implemented")
        }

        override fun getUsername(): String {
            return name
        }

        override fun isAccountNonExpired(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isAccountNonLocked(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isCredentialsNonExpired(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isEnabled(): Boolean {
            TODO("Not yet implemented")
        }

    }

}