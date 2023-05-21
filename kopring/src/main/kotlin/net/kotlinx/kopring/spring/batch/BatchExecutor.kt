package net.kotlinx.kopring.spring.batch

import mu.KotlinLogging
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
    var name = this::class.simpleName

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
                1 -> doWrite()
                else -> doExecute()
            }
        } finally {
            itemReader.closeIfAble()
            itemWriter.closeIfAble()
        }
    }

    /** 일단 잘 동작하니 그냥 둔다 */
    private fun doWrite() {
        var list: MutableList<Any> = mutableListOf()
        while (!Thread.currentThread().isInterrupted) {
            var item: Any = itemReader.read() ?: break
            item = itemProcessor(item)
            list.add(item)
            val size = list.size
            if (size >= commitInterval) {
                itemWriter.write(list as List<Nothing>) //편의상 이렇게 캐스팅
                updateCounter(size)
                list = mutableListOf()
            }
        }
        itemWriter.write(list as List<Nothing>)
        updateCounter(list.size)
    }

    private fun updateCounter(size: Int) {
        val cnt = counter.addAndGet(size.toLong())
        executionContext.putLong(SUM_OF_ITEM, cnt)
    }

    /** 나중에 하자 */
    private fun doExecute(): ExecutionContext {
        throw UnsupportedOperationException()
//        val success = AtomicBoolean(Boolean.TRUE)
//        val executor: ThreadExecutor = ThreadExecutor.of(threadCnt, name)
//        executor.setWaitForTasksToCompleteOnShutdown(false) //스프링 배치 형태는 셧다운(예외 발생)시 모든 스래드를 인터럽트 하고 기다린다.
//        executor.setErrorHandler { e ->
//            if (e is BatchExecutorStopException) {
//                log.info("[{}] BatchExecutorStopException => {}", name, e.getMessage())
//            } else {
//                errorHandler?.handleError(e)
//                val knownStop = e is PropagatedRuntimeException.InterruptedRuntimeException || e is InterruptedException
//                if (knownStop) {
//                    //스택트레이스는 하나면 충분하다.
//                    log.error("[{}] 실행중인 스래드 중단요청으로 stop : {}", Thread.currentThread().name, ExceptionUtil.toString(e))
//                } else {
//                    log.error("[{}] 실행중인 스래드에서 예외발생 => 스래드 전체를 셧다운합니다.", Thread.currentThread().name, e)
//                }
//                executor.shutdown() //즉시 인터럽트함.
//                success.set(Boolean.FALSE)
//            }
//        }
//        val callable = Callable<Long> {
//            try {
//                val countSum = MutableLong()
//                val list = doWrite(countSum)
//                val size = list.size
//                countSum.add(size.toLong())
//                return@Callable countSum.toLong()
//            } finally {
//                BatchExecutorHolder.LOCAL.remove()
//            }
//        }
//        val futures: MutableList<Future<Long>> = Lists.newArrayList()
//        for (i in 0 until threadCnt) {
//            futures.add(executor.submit(callable))
//        }
//        try {
//            //여기서 블럭한다.  인터럽트도 안되고, ExecutionException를 던지지 않는다. (내부 예외 처리)
//            val sum: Long = ThreadUtil.sumIgnoreException(futures)
//            executionContext.putLong(TOTAL_THREAD_COUNT_SUM, sum)
//            SpringBatchUtil.afterStepIfAble(itemReader, sec)
//            SpringBatchUtil.afterStepIfAble(itemProcessor, sec)
//            SpringBatchUtil.afterStepIfAble(itemWriter, sec)
//        } finally {
//            log.trace("리더/라이터 close...")
//            executor.shutdown() //혹시나 넣었다.
//            Preconditions.checkState(executor.getActiveCount() === 0) //모든 스래드가 종료된 이후에 close 된다.
//            SpringBatchUtil.closeIfAble(itemReader)
//            SpringBatchUtil.closeIfAble(itemWriter)
//            //if(threadLocalHold!=null) threadLocalHold.close();
//            log.trace("리더/라이터 close 완료")
//        }
//        check(success.get()) { "병렬 처리시 예외가 발생 -> 메인 스레드에서 예외 전파시킴" }
        return executionContext
    }

    companion object {
        const val SUM_OF_ITEM = "sumOfItem"
    }
}
