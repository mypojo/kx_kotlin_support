package net.kotlinx.concurrent

import kotlinx.coroutines.delay
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.time.measureTimeString
import java.util.concurrent.Callable
import kotlin.time.Duration.Companion.seconds

class CoroutineSupportKt_기본테스트 : BeSpecLog() {
    init {

        initTest(KotestUtil.SLOW)

        Given("기본적인 코루틴") {
            Then("maxConcurrency 조절 -> 걸린시간 측정") {

                val datas = listOf(2, 4, 1, 5, 2).map {
                    suspend {
                        log.trace { " -> [$it] 실행대기..." }
                        delay(it.seconds.inWholeMilliseconds)
                        log.trace { " -> [$it] 실행종료" }
                        "$it 초 딜레이"
                    }
                }

                mapOf(
                    10 to 5.seconds,
                    3 to 6.seconds,
                    2 to 8.seconds,
                ).entries.map { (maxConcurrency, timeout) ->
                    Callable {
                        measureTimeString {
                            //내부는 코루틴으로 작동
                            datas.coroutineExecute(maxConcurrency)
                        }.also {
                            log.info { " => maxConcurrency $maxConcurrency -> 걸린시간 : $it " }
                            check(it.millis <= timeout.inWholeMilliseconds + 200) { " 타임아웃 초과!!" } //버퍼 추가
                        }
                    }
                }.parallelExecute() //각 항목들은 스래드로 측정
            }
        }

    }
}