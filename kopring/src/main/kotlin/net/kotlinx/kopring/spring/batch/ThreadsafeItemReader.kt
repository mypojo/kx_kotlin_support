package net.kotlinx.kopring.spring.batch

import org.springframework.batch.item.ItemReader

/** 간단 변환 */
fun <T> ItemReader<T>.toThreadSafe(): ThreadsafeItemReader<T> = ThreadsafeItemReader(this)

/**
 * 스래드 안전하게 간단 위임
 */
class ThreadsafeItemReader<T>(
    private val delegate: ItemReader<T>
) : ItemReader<T> {

    /** 이부분을 스래드 세이프하게 변경  */
    @Synchronized
    override fun read(): T {
        return delegate.read()
    }
}
