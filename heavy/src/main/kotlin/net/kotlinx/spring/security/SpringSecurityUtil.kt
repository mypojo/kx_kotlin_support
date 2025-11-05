package net.kotlinx.spring.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.security.core.context.SecurityContextHolder


/**
 * 컨텍스트 유지하는 병렬 도구
 * @see net.kotlinx.concurrent.ThreadUtil
 * */
object SpringSecurityUtil {

    /** 간단한 코루틴작업 & 컨텍스트 유지 작업 */
    fun fire(block: suspend () -> Unit) {
        val data = SecurityContextHolder.getContext().authentication
        GlobalScope.launch(Dispatchers.IO) {
            try {
                SecurityContextHolder.getContext().authentication = data
                block()
            } finally {
                SecurityContextHolder.getContext().authentication = null
            }
        }
    }
}
