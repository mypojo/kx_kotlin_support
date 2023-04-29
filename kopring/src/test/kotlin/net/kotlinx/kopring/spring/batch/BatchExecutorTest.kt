package net.kotlinx.kopring.spring.batch

import net.kotlinx.core2.test.TestRoot
import net.kotlinx.kopring.spring.batch.component.toItemReader
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