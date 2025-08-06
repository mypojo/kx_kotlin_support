package net.kotlinx.spring.batch.component

import org.springframework.batch.item.ItemReader

/**
 * IteratorItemReader가 이미 존재하지만 스래드 세이프하지도 않고 생성자가 이상해서 새로 만들었다.
 */
class IteratorItemReader<T>(
    val it: Iterator<T>
) : ItemReader<T> {

    @Synchronized
    override fun read(): T? {
        return if (it.hasNext()) it.next() else null
    }
}

/** 간단 변환 */
fun <T> Iterable<T>.toItemReader(): ItemReader<T> = IteratorItemReader(this.iterator())
