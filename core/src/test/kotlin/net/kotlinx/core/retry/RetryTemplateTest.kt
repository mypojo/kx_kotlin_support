package net.kotlinx.core.retry

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.kotlinx.core.concurrent.coroutineExecute
import net.kotlinx.core.exception.KnownException
import net.kotlinx.test.TestLevel02
import net.kotlinx.test.TestRoot
import java.io.IOException
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class RetryTemplateTest : TestRoot() {

    val CRW: RetryTemplate = RetryTemplate {
        interval = 0.5.seconds
    }

    @TestLevel02
    fun test() {

        try {
            runBlocking {
                CRW.withRetry {
                    println("==== 리트라이 안함")
                    throw IllegalStateException()
                }
            }
        } catch (e: IllegalStateException) {
            println("정상..")
        }

        var cnt = 0
        try {
            runBlocking {
                CRW.withRetry {
                    cnt++
                    println("==== $cnt")
                    throw IllegalStateException(IOException("xxx"))
                }
            }
        } catch (e: IllegalStateException) {
            check(cnt == 4)
        }


    }

    @TestLevel02
    fun `코루틴 테스트`() {

        runBlocking {

            val sum = (0..10).map {
                suspend {
                    CRW.withRetry {
                        delay(2)
                        if (Random.nextInt(2) == 0) throw KnownException.ItemRetryException("명령 $it 다시해!!")
                    }
                    it
                }
            }.coroutineExecute().sum()
            log.warn { "처리결과 : $sum" }


        }

    }

}