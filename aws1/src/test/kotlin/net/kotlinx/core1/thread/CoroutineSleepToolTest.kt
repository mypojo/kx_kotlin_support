package net.kotlinx.core1.thread

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.TestRoot
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

internal class CoroutineSleepToolTest : TestRoot() {

    @Test
    fun `기본테스트`() {

        runBlocking {
            (1..3).forEach { sec ->
                launch {
                    val sleepTool = CoroutineSleepTool(TimeUnit.SECONDS.toMillis(sec.toLong()))

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