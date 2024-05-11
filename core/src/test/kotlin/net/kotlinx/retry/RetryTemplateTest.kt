package net.kotlinx.retry

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.concurrent.delay
import net.kotlinx.exception.KnownException
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.time.TimeStart
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class RetryTemplateTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("RetryTemplate") {

            val retry = RetryTemplate {
                interval = 0.2.seconds
            }

            Then("정해진 예외가 아니라면 리트라이 하지 않음") {
                shouldThrow<IllegalStateException> {
                    retry.withRetry {
                        throw IllegalStateException()
                    }
                }
            }

            Then("리트라이수 +1(최초실행) 만큼 실행되어야함") {
                var cnt = 0
                shouldThrow<IllegalStateException> {
                    retry.withRetry {
                        cnt++
                        throw IllegalStateException(IOException("xxx"))
                    }
                }
                cnt shouldBe retry.retries + 1
            }

            val delayTime = 2.seconds
            val inRange = delayTime.inWholeMilliseconds..(delayTime + 4.seconds).inWholeMilliseconds
            Then("코루틴에서도 정상 작동 -> 로직이 $inRange 안에 종료 되어야함") {
                val start = TimeStart()
                val sum = (0..10).map {
                    suspend {
                        var cnt = 0
                        retry.withRetry {
                            delayTime.delay()
                            cnt++
                            if (cnt <= 1) throw KnownException.ItemRetryException("명령 $it 다시해!!")
                        }
                        it
                    }
                }.coroutineExecute().sum()
                log.info { "처리결과 : $sum -> $start" }
                start.interval() shouldBeInRange inRange
            }
        }
    }
}