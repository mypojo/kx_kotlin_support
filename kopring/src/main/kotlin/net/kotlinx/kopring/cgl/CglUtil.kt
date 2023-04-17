package net.kotlinx.kopring.cgl

import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor

/**
 * LazyLoader할때 역시 사용된다.
 * 거의 Hibernate랑 같이 사용됨.
 * 걍 레퍼런스용
 */
object CglUtil {

    /** 익명/보통 클래스 둘다 구현함 */
    fun <T> makeProxy(target: T, methodInterceptor: MethodInterceptor): T {
        val clazz = target!!::class.java.targetClass
        return makeProxy(clazz, methodInterceptor)
    }

    /** 대상 클래스로 프록시 생성 예제  */
    fun <T> makeProxy(clazz: Class<*>, methodInterceptor: MethodInterceptor): T = Enhancer().apply {
        when {
            clazz.isAnonymousClass -> setInterfaces(clazz.interfaces) //익명클래스는 인터페이스를 사용해야 한다
            else -> setSuperclass(clazz)
        }
        setCallback(methodInterceptor)
    }.create() as T

}
