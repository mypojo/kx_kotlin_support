package net.kotlinx.spring.batch.component

import net.kotlinx.spring.batch.closeIfAble
import net.kotlinx.spring.batch.openIfAble
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemWriter


/**
 * 위임 객체의 결과를 커스텀해주는 위임 라이터
 * 청크 단위 처리를 하기 위해서 만들어졌다. (processor에서는 불가능)
 *  */
class DelegateItemWriter<T>(private val delegate: ItemWriter<T>, private val converter: (List<T>) -> List<T>) : ItemWriter<T>, ItemStream {

    override fun write(chunk: Chunk<out T>) {
        val newItems = converter(chunk.items)
        delegate.write(Chunk(newItems))
    }

    override fun open(executionContext: ExecutionContext) {
        delegate.openIfAble(executionContext)
    }

    override fun update(executionContext: ExecutionContext) {
        //아무것도 안함
    }

    override fun close() {
        delegate.closeIfAble()
    }


}