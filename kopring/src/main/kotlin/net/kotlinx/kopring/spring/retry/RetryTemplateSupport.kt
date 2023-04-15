package net.kotlinx.kopring.spring.retry

import org.springframework.retry.RecoveryCallback
import org.springframework.retry.support.RetryTemplate

/** 리트라이 호출 간단버전 */
inline fun <T> RetryTemplate.withRetry(crossinline retryCallback: () -> T, recoveryCallback: RecoveryCallback<T>? = null): T =
    this.execute<T, Throwable>({ retryCallback.invoke() }, recoveryCallback)

/** CGL 프록시 버전  */
//inline fun <T> RetryTemplate.proxy(target: T): T{
//    return CglUtil.makeProxy(target) { proxy, method, args, proxyMethod ->
//        doWithRetry(RetryCallback<T, E> { arg0: RetryContext? ->
//            try {
//                method.setAccessible(true) //혹시나 해서 추가 : IllegalAccessException : Class com.epe.util.spring.batch.tool.SpringRetryConfig$6$1 can not access a member of class java.lang.Object with modifiers "protected"
//                return@doWithRetry method.invoke(target, args)
//            } catch (e: InvocationTargetException) {
//                throw (e.cause as Exception?)!!
//            }
//        })
//}
