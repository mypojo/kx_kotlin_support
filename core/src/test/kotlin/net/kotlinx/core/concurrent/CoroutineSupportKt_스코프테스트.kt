package net.kotlinx.core.concurrent

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class CoroutineSupportKt_스코프테스트 : TestRoot() {

    @Test
    fun `동시실행 테스트 - runBlocking 또쓰면안됨`() {

        runBlocking {

            val aa = this

            launch {
                log.info { "실행 A" }


                launch {
                    runBlocking {
                        launch {
                            listOf("멍멍", "야옹").map {
                                suspend {
                                    log.info { "실행 $it .." }
                                    3.seconds.delay()
                                }
                            }.coroutineExecute(aa)
                        }
                    }
                }



                log.info { "대기..." }
                3.seconds.delay()

            }
            launch {
                log.info { "실행 B" }
                2.seconds.delay()
            }

        }
        //5초 넘게 걸림

    }

    @Test
    fun `동시실행 테스트 - 스코프 연결`() {

        runBlocking {

            launch {
                log.info { "실행 A" }

                listOf("멍멍", "야옹").map {
                    suspend {
                        log.info { "실행 $it .." }
                        3.seconds.delay()
                    }
                }.coroutineExecute(this)
            }
            launch {
                log.info { "실행 B" }
                2.seconds.delay()
            }

        }

    }


}