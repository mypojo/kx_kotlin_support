package net.kotlinx.aws.kinesis.worker

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.concurrent.delay
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.padStart
import net.kotlinx.time.TimeStart
import net.kotlinx.time.toKr01
import java.time.LocalDateTime
import java.util.concurrent.Callable
import kotlin.time.Duration.Companion.milliseconds

class KinesisTaskWorker_워커테스트 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("Kinesis 태스크") {

            val workerStream = "${findProfile49}-worker-dev"
            val process: suspend (List<KinesisTaskRecord>) -> Unit = { records ->
                val start = TimeStart()
                records.forEach {
                    it.result.put("processed", true)
                    it.result.put("time", LocalDateTime.now().toKr01())
                    log.trace { " -> ${it.result}" }
                    500.milliseconds.delay()
                }
                log.info { "워커 테스트: ${records.size}개의 레코드 처리 -> $start" }
            }

            When("워커 시작 - 1개 & 샤드전체") {
                val worker = KinesisWorker {
                    aws = aws49
                    streamName = workerStream
                    checkpointTableName = "system-dev"
                    readerName = "worker-all"
                    handler = process
                    readChunkCnt = 4  //일부러 쩗게 잡음
                }
                worker.start()
                //57개의 레코드 처리완 -> 19초 (건당 0.3초)
            }

            When("워커 시작 - 1개 & 샤드1개") {
                val worker = KinesisWorker {
                    aws = aws49
                    streamName = workerStream
                    checkpointTableName = "system-dev"
                    readerName = "worker-single"
                    handler = process
                    readChunkCnt = 4  //일부러 쩗게 잡음
                    shardOption = KinesisReader.KinesisReaderShardOption.KinesisReaderShardPartial(2, 0)
                }
                worker.start()
                //
            }

            When("워커 시작 - n개") {
                (0 until 2).map { serverIndex ->
                    Callable {
                        val worker = KinesisWorker {
                            aws = aws49
                            streamName = workerStream
                            checkpointTableName = "system-dev"
                            readerName = "worker-${serverIndex.padStart(2)}"
                            shardOption = KinesisReader.KinesisReaderShardOption.KinesisReaderShardPartial(2, serverIndex)
                            handler = process
                            readChunkCnt = 4  //일부러 쩗게 잡음
                        }
                        runBlocking { worker.start() }
                    }
                }.parallelExecute()
            }

        }
    }

}