package net.kotlinx.spring.batch

import net.kotlinx.spring.batch.component.toItemReader
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import org.springframework.batch.item.ItemWriter

class BatchExecutorTest : TestRoot() {

    @Test
    fun `싱글스래드 테스트`() {
        BatchExecutor {
            itemReader = (0..10).toItemReader()
            itemProcessor = { "N${it}" }
            itemWriter = ItemWriter { items ->
                log.info { "데이터 처리 : $items" }
            }
            commitInterval = 3
        }.also {
            log.info { "결과 컨텍스트 ${it.executionContext}" }
        }
    }


}