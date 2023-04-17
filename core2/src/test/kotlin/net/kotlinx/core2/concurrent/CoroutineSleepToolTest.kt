package net.kotlinx.core2.concurrent

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit.SECONDS

internal class CoroutineSleepToolTest : TestRoot() {

    @Test
    fun `기본테스트`() {

        runBlocking {
            (1..3).forEach { sec ->
                launch {
                    val sleepTool = CoroutineSleepTool(SECONDS.toMillis(sec.toLong()))

                    for (i in (1..3)) {
                        sleepTool.checkAndSleep()
                        log.debug { "$sec delay query -> $i 회 실행 : ${Thread.currentThread().id}" }
                    }

                }

            }
        }
        log.info { "runBlocking 종료" }
    }

}