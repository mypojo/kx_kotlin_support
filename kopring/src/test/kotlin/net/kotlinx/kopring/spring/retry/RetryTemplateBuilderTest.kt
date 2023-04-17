package net.kotlinx.kopring.spring.retry

import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class RetryTemplateBuilderTest : TestRoot() {

    @Test
    fun test() {

        val retry = RetryTemplateBuilder().maxAttempts(3, IllegalStateException::class.java).backoff(1.seconds.inWholeMilliseconds).build()

        (0..5).forEach { c ->
            log.info { "[$c] 테스팅.. " }
            retry.withRetry {
                val num = Random.nextInt(2)
                log.debug { " -> [$c] 주사위 굴림 $num " }
                if (num == 0) throw IllegalStateException("주사위 실패!!")
            }
        }


    }

}