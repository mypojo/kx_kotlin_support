package net.kotlinx.core.concurrent

/**
 * 생성자가 있는 스래드로컬
 * ex) ThreadSleepData{ mutableSetOf<String>() }
 */
class ThreadData<T>(
    private val supplier: () -> T,
) {

    /** 이걸 감추는게 의도 */
    private val threadLocal = ThreadLocal<T?>()

    fun get() = threadLocal.get() ?: supplier().also { threadLocal.set(it) }

    fun remove() = threadLocal.remove()

}