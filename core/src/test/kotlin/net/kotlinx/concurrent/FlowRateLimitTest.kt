package net.kotlinx.concurrent

import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.flow
import net.kotlinx.flow.rateLimit
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.number.size
import net.kotlinx.time.toTimeString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class FlowRateLimitTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("rateLimit extension function") {

            When("기본 rate limiting 테스트") {

                Then("1초에 x개씩 제한") {
                    val startTime = System.currentTimeMillis()
                    val list = 1..4
                    val permits = 2
                    val period = 1.seconds

                    list.asFlow().rateLimit(permits, period).collect { item -> log.debug { "처리된 아이템: $item" } }
                    val interval = System.currentTimeMillis() - startTime
                    // 6개 아이템을 2개씩 처리하면 최소 2초는 걸려야 함
                    interval shouldBeLessThan list.size * 1000L
                    log.info { "총 소요시간: ${interval.toTimeString()}" }
                }

                Then("1초에 x개씩 제한 -> 청크 단위로 rateLimit 가 걸린다") {
                    val startTime = System.currentTimeMillis()
                    val list = 1..7
                    val permits = 1
                    val period = 1.seconds

                    list.asFlow().chunked(3).rateLimit(permits, period).collect { item -> log.debug { "처리된 아이템: $item" } }
                    val interval = System.currentTimeMillis() - startTime
                    // 6개 아이템을 2개씩 처리하면 최소 2초는 걸려야 함
                    interval shouldBeLessThan list.size * 1000L
                    log.info { "총 소요시간: ${interval.toTimeString()}" }
                }
            }

            When("빠른 속도로 방출되는 Flow") {
                Then("rate limit 적용 확인") {
                    val startTime = System.currentTimeMillis()
                    val results = mutableListOf<Pair<Int, Long>>()

                    flow {
                        repeat(5) {
                            emit(it + 1)
                        }
                    }.rateLimit(2, 1.seconds).collect { item ->
                        val timestamp = System.currentTimeMillis() - startTime
                        results.add(item to timestamp)
                        log.debug { "아이템 $item 처리됨 - ${timestamp}ms" }
                    }

                    // 첫 2개는 즉시, 나머지는 1초 간격으로 처리되어야 함
                    results.size shouldBe 5
                    results[2].second shouldBeGreaterThanOrEqual 1000L
                    results[4].second shouldBeGreaterThanOrEqual 2000L
                }
            }

            When("permits가 1인 경우") {
                Then("각 아이템이 period 간격으로 처리됨") {
                    val items = (1..3).asFlow()
                    val permits = 1
                    val period = 500.milliseconds

                    val startTime = System.currentTimeMillis()

                    items.rateLimit(permits, period).collect { item ->
                        log.debug { "순차 처리: $item" }
                    }

                    val interval = System.currentTimeMillis() - startTime
                    // 3개 아이템을 500ms 간격으로 처리하면 최소 1초는 걸려야 함
                    interval shouldBeGreaterThanOrEqual 1000L
                    log.info { "순차 처리 소요시간: ${interval.toTimeString()}" }
                }
            }
        }
    }
}