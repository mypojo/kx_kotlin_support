package net.kotlinx.spring.retry

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class RetryTemplateBuilderTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("RetryTemplateBuilder") {

            log.warn { "스프링 리트라이는 가급적 쓰지마세요~" }

            Then("리트라이 테스트") {
                val retry = RetryTemplateBuilder().maxAttempts(10, IllegalStateException::class.java).backoff(200.milliseconds).build()
                (0 until 4).forEach { c ->
                    log.info { "[$c] 테스팅.. " }
                    retry.withRetry {
                        val num = Random.nextInt(2)
                        log.debug { " -> [$c] 주사위 굴림 $num " }
                        if (num == 0) throw IllegalStateException("주사위 실패!! -> 재시도함")
                    }
                }
            }
        }
    }


}