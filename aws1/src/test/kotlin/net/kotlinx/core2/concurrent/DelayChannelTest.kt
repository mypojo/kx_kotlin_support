package net.kotlinx.core2.concurrent

import ch.qos.logback.classic.Level
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.kotlinx.core1.CoreUtil
import net.kotlinx.core2.logback.LogBackUtil
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

internal class DelayChannelTest : DescribeSpec({

    val log = KotlinLogging.logger {}
    LogBackUtil.logLevelTo(CoreUtil.packageName, Level.DEBUG)

    val randomRange = 100L..500L
    describe("기본기능") {
        context("단건 처리 샘플") {
            it("단건 처리 - 합계 일치") {
                val sumOfSend = AtomicLong()
                val sumOfReceive = AtomicLong()
                coroutineScope {
                    val channel = DelayChannel(this, maxConcurrency = 100)
                    channel.startMonitoring(TimeUnit.SECONDS.toMillis(5))
                    launch {
                        repeat(2) {
                            log.debug { "데이터 입력.." }
                            repeat((0 until 200).count()) {
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
                assert(sumOfSend.get() == sumOfReceive.get()) { "모든 데이터가 처리되어야함" }
                log.info { "요청합계 $sumOfSend / 결과합계 $sumOfReceive" }
            }
        }
        context("배치 처리 샘플") {
            it("배치 처리 - 합계 일치") {
                val sumOfSend = AtomicLong()
                val sumOfReceive = AtomicLong()
                coroutineScope {
                    val channel = DelayChannel(this, maxConcurrency = 100)
                    channel.startMonitoring(TimeUnit.SECONDS.toMillis(5))
                    launch {
                        repeat(2) {
                            log.debug { "데이터 입력.." }
                            repeat((0 until 200).count()) {
                                val num = randomRange.random()
                                channel.send(num.toString())
                                sumOfSend.addAndGet(num)
                            }
                            delay(2000)
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
                assert(sumOfSend.get() == sumOfReceive.get()) { "모든 데이터가 처리되어야함" }
                log.info { "요청합계 $sumOfSend / 결과합계 $sumOfReceive" }
            }
        }
    }

    describe("옵션 기능") {
        context("maxConcurrency 설정") {
            it("maxConcurrency 이상으로 작동하지 않음") {
                val maxConcurrency = 3
                val channel = DelayChannel(this, maxConcurrency = maxConcurrency)
                channel.startMonitoring(TimeUnit.SECONDS.toMillis(5))
                val currentConcurrency = AtomicLong()
                launch {
                    repeat((0 until 10).count()) { channel.send(randomRange.random().toString()) }
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
        context("timeout 설정") {
            it("timeout 이상시 예외") {
                val channel = DelayChannel(this, timeout = TimeUnit.SECONDS.toMillis(2), exCallback = {
                    println(it)
                    throw it
                })
                launch {
                    repeat((0 until 10).count()) { channel.send(randomRange.random().toString()) }
                    channel.close()
                }

//                try {
//                    launch {
//                        try {
//                            withTimeout(2000){
//                                launch {
//                                    delay(TimeUnit.SECONDS.toMillis(5))
//                                    println("무거운 작업 종료")
//                                }
//                                launch {
//                                    println("aaa")
//                                    delay(TimeUnit.SECONDS.toMillis(3))
//                                    println("bbb")
//                                }
//                            }
//                        } catch (e: Exception) {
//                            println("이게잡혀야함 $e")
//                        }
//                    }
//                } catch (e: Exception) {
//                    println(e)
//                }


                println("bb")
                channel.receive {
                    println("무거운거 시작..")
                    delay(TimeUnit.SECONDS.toMillis(5))
                    println("무거운거 종료")
                }
            }
        }
    }
})