package net.kotlinx.spring

import jakarta.servlet.http.HttpServletRequest
import net.kotlinx.spring.security.SimpleAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


/**
 * 스프링에서 제공하는 홀더 래핑
 */
object SpringHolder {

    //==================================================== 이미 준비됨 ======================================================

    /**
     * http request는 이미 스래드로컬에 있음
     * ex) httpRequest.forwardedIp
     * */
    val httpRequest: HttpServletRequest
        get() = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request


    fun <T : UserDetails> userDetail(): T? {
        val authenticationToken = SecurityContextHolder.getContext().authentication as SimpleAuthenticationToken? ?: return null
        return authenticationToken.principal as T
    }


}