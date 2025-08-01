package net.kotlinx.aws.kinesis.worker

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.kinesis.reader.KinesisIdleChecker
import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.aws.kinesis.writer.KinesisWriter
import net.kotlinx.concurrent.coroutine
import net.kotlinx.concurrent.delay
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.csv.toFlow
import net.kotlinx.file.slash
import net.kotlinx.io.input.toInputResource
import net.kotlinx.json.gson.json
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.lazyLoad.lazyLoad
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.toKr01
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * KinesisTask 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. A 파티션에 데이터 100개 넣기
 * 2. B 파티션에서 응답 100개 받기
 * 3. 모든 응답을 받으면 완료 처리
 */
class KinesisTaskWorkerTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("Kinesis 태스크") {

            val workerStream = "${findProfile49}-worker-dev"

            When("워커 시작") {

                // 워커 설정 (in 타입 데이터를 처리하고 out 타입으로 전송하는 역할)
                val worker = KinesisWorker {
                    aws = aws49
                    streamName = workerStream
                    checkpointTableName = "system-dev"
                    handler = { records ->
                        log.info { "워커 테스트: ${records.size}개의 레코드 처리" }
                        records.forEach {
                            it.result.put("processed", true)
                            it.result.put("time", java.time.LocalDateTime.now().toKr01())
                            log.debug { " -> ${it.result}" }
                            500.milliseconds.delay()
                        }
                    }
                    readChunkCnt = 4  //일부러 쩗게 잡음
                    shardCheckInterval = 10.minutes
                }
                worker.start()
            }

            When("idle 테스트") {

                val writer = KinesisWriter {
                    aws = aws49
                    streamName = workerStream
                    partitionKeyBuilder = { it["id"].str!! }
                    maxRetries = 3
                }

                val idleChecker = KinesisIdleChecker {
                    idleCallback = {
                        val itemns = (0 until 10).map {
                            json {
                                "id" to "${it}"
                                "value" to "value${it}"
                            }
                        }
                        writer.putRecords(itemns)
                        log.info { "idle 감지 -> ${itemns.size}건의 데이터 입력" }
                    }
                }

                val reader = KinesisReader {
                    aws = aws49
                    streamName = workerStream
                    readerName = "idle-test"
                    checkpointTableName = "system-dev"
                    recordCheckInterval = 10.seconds
                    shardCheckInterval = 10.minutes
                    recordHandler = idleChecker
                    recordEmptyHandler = idleChecker
                }
                reader.start()
            }

            When("task") {

                val task = KinesisTask {
                    aws = aws49
                    streamName = workerStream
                    checkpointTableName = "system-dev"
                    taskName = "demoTaskJob"
                    checkpointTtl = 1.hours
                    timeout = 1.hours
                }

                Then("task 병렬처리 테스트 (필수 테스트)") {

                    val tasks = listOf(
                        "task01" to 15,
                        "task02" to 8,
                        "task03" to 30,
                    )

                    val cnt = AtomicInteger()
                    //코루틴이 아닌 스래드 병렬처리
                    tasks.map { e ->
                        Callable {
                            runBlocking {
                                val flowIn = (0 until e.second).map {
                                    json {
                                        "id" to "${e.first}-${it}"
                                        "value" to "value${it}"
                                    }
                                }.asFlow().chunked(10).onEach { delay(100) }
                                val flowOut = task.execute(flowIn)
                                flowOut.collect { datas ->
                                    datas.forEach {
                                        log.debug { " => [${e.first}] -> ${it}" }
                                        cnt.incrementAndGet()
                                    }
                                }
                            }
                        }
                    }.parallelExecute()
                    tasks.map { it.second }.sum() shouldBe cnt.get()
                    log.info { "### 전체 로직 ${cnt.get()}건 정상처리 완료" }
                }

                Then("task 싱글 테스트") {
                    val tasks = listOf(
                        "task01" to 15,
                    )
                    tasks.coroutine { e ->
                        val flowIn = (0 until e.second).map {
                            json {
                                "id" to "${e.first}-${it}"
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
                        } catch (e: TimeoutCancellationException) {
                            log.warn { "타임아웃으로 인한 테스트 중단!!" }
                        }
                    }
                }

            }

            xWhen("task 코드 demo (실행안됨)") {

                val task = KinesisTask {
                    aws = aws49
                    streamName = workerStream
                    checkpointTableName = "system-dev"
                    taskName = "demoTaskJob"
                    checkpointTtl = 1.hours
                }
                val file: File by ResourceHolder.WORKSPACE.slash("largeFile.csv") lazyLoad "s3://xxxa/demo/largeFile.csv"
                val flow = file.toInputResource().toFlow()
                    .map { line ->
                        json {
                            "id" to line[0]
                            "query" to line[1]
                        }
                    }
                    .chunked(1000)
                task.execute(flow).collect { datas ->
                    datas.forEach {
                        log.debug { " => [${it}]" }
                    }
                }
            }
        }
    }

}