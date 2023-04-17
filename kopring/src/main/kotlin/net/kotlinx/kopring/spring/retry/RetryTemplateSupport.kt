package net.kotlinx.kopring.spring.retry

import net.kotlinx.kopring.cgl.CglUtil
import org.springframework.retry.RecoveryCallback
import org.springframework.retry.support.RetryTemplate
import java.lang.reflect.InvocationTargetException

/** 리트라이 호출 간단버전 */
inline fun <T> RetryTemplate.withRetry(recoveryCallback: RecoveryCallback<T>? = null, crossinline retryCallback: () -> T): T =
    this.execute<T, Throwable>({ retryCallback.invoke() }, recoveryCallback)

/** CGL 프록시 버전  */
inline fun <T> RetryTemplate.proxy(target: T): T = CglUtil.makeProxy(target) { _, method, args, _ ->
    try {
        method.isAccessible = true //혹시나 해서 추가
        return@makeProxy method.invoke(target, args)
    } catch (e: InvocationTargetException) {
        throw e.cause!! //예외 발생시 원본 오류를 리턴해준다.
    }
}

