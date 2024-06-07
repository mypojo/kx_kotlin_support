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
     * 스프링 MVC HttpServletRequest
     * ex) HTTP_REQUEST.forwardedIp
     * */
    val HTTP_REQUEST: HttpServletRequest
        get() = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request


    /**
     * 스프링 시큐리티 UserDetails
     *  */
    fun <T : UserDetails> userDetail(): T? {
        val authenticationToken = SecurityContextHolder.getContext().authentication as SimpleAuthenticationToken? ?: return null
        return authenticationToken.principal as T
    }

    //==================================================== 커스텀 ======================================================

    /**
     * 늦은 초기화 이후 사용할때.
     * */
    lateinit var BEANS: SpringBootBeans


}