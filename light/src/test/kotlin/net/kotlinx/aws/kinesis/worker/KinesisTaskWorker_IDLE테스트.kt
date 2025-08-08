package net.kotlinx.aws.kinesis.worker

import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.map
import net.kotlinx.aws.kinesis.reader.KinesisIdleChecker
import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.aws.kinesis.writer.KinesisWriteData
import net.kotlinx.aws.kinesis.writer.KinesisWriter
import net.kotlinx.csv.toFlow
import net.kotlinx.file.slash
import net.kotlinx.io.input.toInputResource
import net.kotlinx.json.gson.json
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.lazyLoad.lazyLoad
import net.kotlinx.system.ResourceHolder
import java.io.File
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class KinesisTaskWorker_IDLE테스트 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("Kinesis 태스크") {

            val workerStream = "${findProfile49}-worker-dev"

            When("idle 테스트") {

                val writer = KinesisWriter {
                    aws = aws49
                    streamName = workerStream
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
                        val records = itemns.map { KinesisWriteData(it["id"].str!!, it) }
                        writer.putRecords(records)
                        log.info { "idle 감지 -> ${itemns.size}건의 데이터 입력" }
                    }
                }

                val reader = KinesisReader {
                    aws = aws49
                    streamName = workerStream
                    readerName = "idle-test"
                    checkpointTableName = "system-dev"
                    recordCheckInterval = 10.seconds
                    recordHandler = idleChecker
                    recordEmptyHandler = idleChecker
                }
                reader.start()
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