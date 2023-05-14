package net.kotlinx.core.retry

import kotlinx.coroutines.runBlocking
import net.kotlinx.core2.test.TestLevel02
import net.kotlinx.core2.test.TestRoot
import java.io.IOException
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

}