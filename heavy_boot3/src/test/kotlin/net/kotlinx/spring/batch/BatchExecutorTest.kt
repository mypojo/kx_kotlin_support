package net.kotlinx.spring.batch

import io.kotest.matchers.shouldBe
import net.kotlinx.concurrent.sleep
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.spring.batch.component.toItemReader
import org.springframework.batch.item.ItemWriter
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.seconds

class BatchExecutorTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("BatchExecutor") {

            Then("간단 실행 데모 - 싱글스래드") {
                val context = BatchExecutor {
                    itemReader = (0..10).toItemReader()
                    itemProcessor = { "N${it}" }
                    itemWriter = ItemWriter { items ->
                        log.info { "데이터 처리 : $items" }
                    }
                    commitInterval = 3
                }
                log.info { "결과 컨텍스트 ${context.executionContext}" }
            }

            Then("간단 실행 데모 - 병렬스래드") {
                val max = 643
                val sum = AtomicLong()
                val context = BatchExecutor {
                    itemReader = (0..max).toItemReader()
                    itemWriter = ItemWriter { chunk ->
                        log.debug { " -> ${Thread.currentThread().name} -> ${chunk.items}" }
                        val delta = chunk.items.map { it as Int }.sumOf { it }
                        sum.addAndGet(delta.toLong())
                        1.seconds.sleep()
                    }
                    commitInterval = 17
                    threadCnt = 13
                }
                log.info { "결과 컨텍스트 ${context.executionContext} -> sum = ${sum.get()}" }
                sum.get() shouldBe (0 .. max).sum()
            }
        }
    }
}