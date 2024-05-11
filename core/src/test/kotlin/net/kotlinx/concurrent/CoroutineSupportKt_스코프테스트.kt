package net.kotlinx.concurrent

import kotlinx.coroutines.launch
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import kotlin.time.Duration.Companion.seconds

class CoroutineSupportKt_스코프테스트 : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("코루틴 스코프 이해") {
            Then("A -> B -> 내부 -> C") {
                var cnt = 0
                launch {
                    log.debug { "실행 A" }
                    cnt++
                    2.seconds.delay()
                }
                launch {
                    log.debug { "실행 B" }
                    cnt++
                    log.trace { "내부적으로 런블로킹 때문에 여기서 이 런치는 블로킹당함" }
                    listOf("멍멍", "야옹").map {
                        suspend {
                            log.info { " -> 내부실행 $it .." }
                            check(cnt == 2)
                            3.seconds.delay()
                        }
                    }.coroutineExecute() //블로킹

                }
                launch {
                    log.debug { "실행 C" }
                    cnt++
                    2.seconds.delay()
                }
            }
            Then("A -> B -> 내부실행") {
                var scope = this
                launch {
                    log.debug { "실행 A" }
                    log.trace { "내부적으로 런블로킹 때문에 여기서 이 런치는 블로킹 안당함" }
                    1.seconds.delay()
                    listOf("멍멍", "야옹").map {
                        suspend {
                            log.info { " -> 내부실행 $it .." }
                            3.seconds.delay()
                        }
                    }.coroutineExecute(scope) //논블로킹 (스코프 연결)

                }
                launch {
                    log.debug { "실행 B" }
                    2.seconds.delay()
                }
            }
        }
    }

}