package net.kotlinx.core.concurrent

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

internal class CoroutineSleepToolTest : TestRoot() {

    @Test
    fun `기본테스트`() {

        runBlocking {
            (1..3).forEach { sec ->
                launch {
                    val sleepTool = CoroutineSleepTool(sec.seconds)

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