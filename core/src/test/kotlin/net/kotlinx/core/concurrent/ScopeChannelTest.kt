package net.kotlinx.core.concurrent

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kotlinx.core.time.toTimeString
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong


internal class ScopeChannelTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        val randomRange = 100L..500L
        Given("기본기능") {
            When("단건 처리 샘플") {
                Then("단건 처리 - 합계 일치") {
                    val sumOfSend = AtomicLong()
                    val sumOfReceive = AtomicLong()
                    coroutineScope {
                        val channel = ScopeChannel(this, maxConcurrency = 100)
                        channel.startMonitoring(TimeUnit.SECONDS.toMillis(5))
                        launch {
                            repeat(2) {
                                log.debug { "데이터 입력.." }
                                repeat(200) {
                                    val num = randomRange.random()
                                    channel.send(num.toString())
                                    sumOfSend.addAndGet(num)
                                }
                                delay(2000)
                            }
                            log.debug { "channel.close" }
                            channel.close()
                        }
                        channel.receive {
                            delay(randomRange.random() * 10)
                            sumOfReceive.addAndGet(it.toLong())
                        }
                    }
                    check(sumOfSend.get() == sumOfReceive.get()) { "모든 데이터가 처리되어야함" }
                    log.info { "요청합계 $sumOfSend / 결과합계 $sumOfReceive" }
                }
            }
            When("배치 처리 샘플") {
                Then("배치 처리 - 합계 일치") {
                    val sumOfSend = AtomicLong()
                    val sumOfReceive = AtomicLong()
                    coroutineScope {
                        val channel = ScopeChannel(this, maxConcurrency = 100, delay = 1001L)
                        channel.startMonitoring(TimeUnit.SECONDS.toMillis(4))
                        launch {
                            repeat(10) {
                                log.debug { "데이터 입력.." }
                                repeat(200) {
                                    val num = randomRange.random()
                                    channel.send(num.toString())
                                    sumOfSend.addAndGet(num)
                                }
                                delay(500)
                            }
                            log.debug { "channel.close" }
                            channel.close()
                        }
                        channel.receiveBatch { items ->
                            delay(randomRange.random() * 10)
                            log.debug { " -> 데이터 로드 ${items.size}건" }
                            sumOfReceive.addAndGet(items.sumOf { it.toLong() })
                        }
                    }
                    check(sumOfSend.get() == sumOfReceive.get()) { "모든 데이터가 처리되어야함" }
                    log.info { "요청합계 $sumOfSend / 결과합계 $sumOfReceive" }
                }
            }
        }

        Given("옵션 기능") {
            When("maxConcurrency 설정") {
                Then("maxConcurrency 이상으로 작동하지 않음") {
                    val maxConcurrency = 3
                    val channel = ScopeChannel(this, maxConcurrency = maxConcurrency)
                    channel.startMonitoring(TimeUnit.SECONDS.toMillis(5))
                    val currentConcurrency = AtomicLong()
                    launch {
                        repeat(10) { channel.send(randomRange.random().toString()) }
                        channel.close()
                    }
                    channel.receive {
                        val current = currentConcurrency.incrementAndGet()
                        check(current <= maxConcurrency) { "최대 실행 수 이상으로 작동할 수 없음" }
                        log.debug { " -> 데이터 처리중.. $it / $current" }
                        delay(randomRange.random() * 10)
                        currentConcurrency.decrementAndGet()
                    }
                }
            }
            When("timeout 설정") {
                Then("timeout 이상시 예외") {
                    val errCnt = AtomicLong()
                    coroutineScope {
                        val myTimeout = TimeUnit.SECONDS.toMillis(2)
                        val channel = ScopeChannel(this, timeout = myTimeout, timeoutCallback = { e, data ->
                            log.debug { "$e / input = $data" }
                            errCnt.incrementAndGet()
                        })
                        launch {
                            channel.send((myTimeout - 1000).toString())
                            channel.send((myTimeout + 1000).toString())
                            channel.send((myTimeout - 500).toString())
                            channel.send((myTimeout + 500).toString())
                            channel.close()
                        }
                        channel.receive {
                            log.debug { "무거운작업 시작.. $it" }
                            delay(it.toLong())
                            log.debug { "무거운작업 종료.. $it" }
                        }
                    }
                    check(errCnt.get() == 2L)
                }
            }

            When("딜레치 기능(작업간 간격 보장)") {
                Then("일정 딜레이 후 작업이 실행됨") {
                    val start = System.currentTimeMillis()
                    val delayMills = TimeUnit.SECONDS.toMillis(2)
                    val repeat = 4
                    coroutineScope {
                        val channel = ScopeChannel(this, delay = delayMills)
                        launch {
                            repeat(repeat) { channel.send(randomRange.random().toString()) }
                            channel.close()
                        }
                        channel.receive {
                            log.debug { "처리.." }
                            delay(delayMills - 600)
                        }
                    }
                    val interval = System.currentTimeMillis() - start
                    val exptected = delayMills * repeat
                    log.info { "실제걸린시간 : ${interval.toTimeString()} / 보장시간 : ${exptected.toTimeString()}" }
                    check(interval >= exptected) { "작업간의 시간 간격이 보장되어야함" }
                }
            }

        }

        Given("예외 처리") {
            When("캐치되지 않은 예외 -> scope 블록내 처리O") {
                Then("모든 launch 정상실행") {
                    val successCnt = AtomicLong()
                    coroutineScope {
                        val channel = ScopeChannel(this, exCallback = { e, data ->
                            log.warn { "코루틴 중지!! & 예외 무시 [$data] -> $e" }
                        })
                        launch {
                            channel.send("a")
                            channel.send("poison")
                            channel.send("b")
                            delay(1000)
                            channel.send("C")
                            channel.close()
                        }
                        channel.receive {
                            log.debug { "데이터 입력.. $it" }
                            if (it == "poison") throw IllegalStateException("poison!!")
                            delay(1000)
                            delay(2000)
                            successCnt.incrementAndGet()
                            log.debug { "데이터 처리완료 $it" }
                        }
                    }
                    log.info { "정상 처리 건수 $successCnt" }
                    check(successCnt.get() == 3L) { "모든 데이터 정상처리" }
                }
            }
            When("캐치되지 않은 예외 -> scope 블록내 처리X") {
                Then("모든 launch stop") {
                    val successCnt = AtomicLong()
                    try {
                        coroutineScope {
                            val channel = ScopeChannel(this, exCallback = { e, data ->
                                log.warn { "코루틴 중지!! & 예외 던짐 [$data] -> $e" }
                                throw e
                            })
                            launch {
                                channel.send("a")
                                channel.send("poison")
                                channel.send("b")
                                delay(1000)
                                channel.send("C")
                                channel.close()
                            }
                            channel.receive {
                                log.debug { "데이터 입력.. $it" }
                                if (it == "poison") throw IllegalStateException("poison!!")
                                delay(1000)
                                delay(2000)
                                successCnt.incrementAndGet()
                                log.debug { "데이터 처리완료 $it" }
                            }
                        }
                    } catch (e: Exception) {
                        log.warn { "전체 로그 무시" }
                    }
                    log.info { "정상 처리 건수 $successCnt" }
                    check(successCnt.get() == 0L) { "모든 데이터 처리 안됨" }
                }
            }

        }

    }


}