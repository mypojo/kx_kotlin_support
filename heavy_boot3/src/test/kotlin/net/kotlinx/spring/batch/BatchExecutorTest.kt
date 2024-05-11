package net.kotlinx.spring.batch

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.spring.batch.component.toItemReader
import org.springframework.batch.item.ItemWriter

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
        }
    }
}