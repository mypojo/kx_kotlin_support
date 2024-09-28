package net.kotlinx.spring.batch

import mu.KotlinLogging
import net.kotlinx.spring.thread.ThreadPoolBuilder
import net.kotlinx.spring.thread.executeAll
import net.kotlinx.spring.thread.wait
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.util.ErrorHandler
import java.util.concurrent.atomic.AtomicLong

/**
 * 스프링 배치를 간단히 로컬에서 돌려볼 수 있는 테스트기
 * 제너릭 불편해서 다 뺐다.
 * 매 실행시마다 생성해서 사용할것!
 *
 * 설정할게 많음으로 fluent하게 만들지 않는다.
 */
class BatchExecutor(
    block: BatchExecutor.() -> Unit
) {

    private val log = KotlinLogging.logger {}

    //======================= 설정파일 ===============================
    var name: String = this::class.simpleName!!

    lateinit var itemReader: ItemReader<out Any>
    lateinit var itemWriter: ItemWriter<out Any>
    var itemProcessor: (Any) -> Any = { it }

    var threadCnt = 1
    var commitInterval = 1024
    var errorHandler: ErrorHandler? = null

    //======================= 내부 사용 ===============================
    /** 컨텍스트 저장소 */
    val executionContext = ExecutionContext()
    val counter: AtomicLong = AtomicLong(0)

    init {
        block(this)
        check(threadCnt > 0)

        itemReader.openIfAble(executionContext)
        itemWriter.openIfAble(executionContext)

        try {
            when (threadCnt) {
                1 -> doExecute()
                else -> doThreadExecute()
            }
        } finally {
            itemReader.closeIfAble()
            itemWriter.closeIfAble()
        }
    }

    /** 일단 잘 동작하니 그냥 둔다 */
    private fun doExecute(): Int {
        var list: MutableList<Any> = mutableListOf()
        while (!Thread.currentThread().isInterrupted) {
            var item: Any = itemReader.read() ?: break
            item = itemProcessor(item)
            list.add(item)
            val size = list.size
            if (size >= commitInterval) {
                itemWriter.write(Chunk(list) as Chunk<out Nothing>) //나중에 수정.
                updateCounter(size)
                list = mutableListOf()
            }
        }
        itemWriter.write(Chunk(list) as Chunk<out Nothing>)
        updateCounter(list.size)
        return list.size
    }

    private fun updateCounter(size: Int) {
        val cnt = counter.addAndGet(size.toLong())
        executionContext.putLong(SUM_OF_ITEM, cnt)
    }

    /** 나중에 하자 */
    private fun doThreadExecute(): ExecutionContext {
        val executor = ThreadPoolBuilder(threadCnt, name).build {
            setWaitForTasksToCompleteOnShutdown(false) //스프링 배치 형태는 셧다운(예외 발생)시 모든 스래드를 인터럽트 하고 기다린다.
        }
        executor.executeAll { doExecute() }.wait()
        executor.shutdown()
        return executionContext
    }

    companion object {
        const val SUM_OF_ITEM = "sumOfItem"
    }
}
