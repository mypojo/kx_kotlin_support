package net.kotlinx.aws.kinesis.worker

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import net.kotlinx.concurrent.coroutine
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.json.gson.json
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.hours

/**
 * KinesisTask 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. A 파티션에 데이터 100개 넣기
 * 2. B 파티션에서 응답 100개 받기
 * 3. 모든 응답을 받으면 완료 처리
 */
class KinesisTaskWorker_태스크테스트 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("Kinesis 태스크") {

            val workerStream = "${findProfile49}-worker-dev"

            When("task") {

                val task = KinesisTask {
                    aws = aws49
                    streamName = workerStream
                    checkpointTableName = "system-dev"
                    taskName = "workerTestJob"
                    checkpointTtl = 1.hours
                    timeout = 1.hours
                }
                val idGenerator = AtomicInteger()

                Then("task 싱글 테스트 (디버깅용)") {
                    val tasks = listOf(
                        "req01" to 57,
                    )
                    tasks.coroutine { e ->
                        val flowIn = (0 until e.second).map {
                            json {
                                "id" to "${e.first}-${idGenerator.incrementAndGet()}"
                                "value" to "value${it}"
                            }
                        }.asFlow().chunked(10).onEach { delay(100) }

                        val flowOut = task.execute(flowIn)

                        try {
                            flowOut.collect { datas ->
                                datas.forEach {
                                    log.debug { " => [${e.first}] -> ${it}" }
                                }
                            }
                        } catch (_: TimeoutCancellationException) {
                            log.warn { "타임아웃으로 인한 테스트 중단!!" }
                        }
                    }
                }


                Then("task 병렬처리 테스트 (필수 테스트)") {

                    val tasks = listOf(
                        "작업1" to 45,
                        "작업2" to 86,
                        "작업3" to 70,
                    )

                    val testCnt = AtomicInteger()
                    //코루틴이 아닌 스래드 병렬처리
                    tasks.map { e ->
                        Callable {
                            runBlocking {
                                val flowIn = (0 until e.second).map {
                                    json {
                                        "id" to "${idGenerator.incrementAndGet()}"
                                        "value" to "value${it}"
                                    }
                                }.asFlow().chunked(10).onEach { delay(100) }
                                val flowOut = task.execute(flowIn)
                                flowOut.collect { datas ->
                                    datas.forEach {
                                        log.debug { " => 응답 for [${e.first}] -> ${it}" }
                                        testCnt.incrementAndGet()
                                    }
                                }
                            }
                        }
                    }.parallelExecute()
                    tasks.map { it.second }.sum() shouldBe testCnt.get()
                    log.info { "### 전체 로직 ${testCnt.get()}건 정상처리 완료" }
                }
            }
        }
    }

}