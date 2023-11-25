package net.kotlinx.core.prop

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyLoadProperty<T : Any>(val initBlock: () -> T, val clazz: Class<T>) {

    operator fun <R> provideDelegate(ref: R, prop: KProperty<*>): ReadOnlyProperty<R, T> = delegate()

    private fun <R> delegate(): ReadOnlyProperty<R, T> = object : ReadOnlyProperty<R, T> {
        override fun getValue(thisRef: R, property: KProperty<*>): T {
            val hash = clazz.hashCode()
            val cached = LAZY_CACHE[hash]
            if (cached != null && cached.javaClass == clazz) return cached as T
            return initBlock().apply { LAZY_CACHE[hash] = this }
        }
    }
}

private val LAZY_CACHE = HashMap<Int, Any>()

/**
 * private 객체에 접근 해야함으로 reified 를 사용하지 않음
 *  */
fun <T> lzayLoadReset(clazz: Class<T>): Boolean {
    val hash = clazz.hashCode()
    val result = LAZY_CACHE[hash]
    if (result?.javaClass != clazz) return false

    LAZY_CACHE.remove(hash)
    return true
}

/**
 * 리셋이 가능한 늦은 초기화 & 해당 클래스로 캐시 등록을 해준다.
 * 싱글톤 전용임!!  클래스다 다르면 오버라이드됨
 *  */
inline fun <reified T : Any> lazyLoad(noinline block: () -> T): LazyLoadProperty<T> = LazyLoadProperty(block, T::class.java)