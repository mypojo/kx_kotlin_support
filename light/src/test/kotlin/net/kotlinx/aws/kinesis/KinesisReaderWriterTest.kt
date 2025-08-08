package net.kotlinx.aws.kinesis

import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.aws.kinesis.reader.printSimple
import net.kotlinx.aws.kinesis.writer.KinesisWriteData
import net.kotlinx.aws.kinesis.writer.KinesisWriter
import net.kotlinx.json.gson.json
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

/**
 * KinesisPutManager 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. 모든 레코드가 성공적으로 처리되는 경우
 * 2. 일부 레코드가 실패하고 재시도 후 성공하는 경우
 * 3. 일부 레코드가 계속 실패하여 최대 재시도 횟수에 도달하는 경우
 */
class KinesisReaderWriterTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("Kinesis 읽기/쓰기") {

            val workerStreamName = "${findProfile49}-worker-dev"

            // 테스트 데이터 생성 - GsonData 객체 생성
            val datas = listOf(
                json {
                    "id" to "1"
                    "value" to "value1"
                },
                json {
                    "id" to "2"
                    "value" to "value2"
                },
                json {
                    "id" to "3"
                    "value" to "value3"
                },
            )

            When("쓰기작업") {

                // KinesisPutManager 인스턴스 생성
                val writer = KinesisWriter {
                    aws = aws49
                    streamName = workerStreamName
                    maxRetries = 3
                }

                Then("모든 레코드가 성공적으로 처리된다") {
                    val records = datas.map { KinesisWriteData(it["id"].str!!, it) }
                    writer.putRecords(records)
                }
            }

            When("읽기작업") {

                // Consumer 시작
                val consumer = KinesisReader {
                    aws = aws49
                    streamName = workerStreamName
                    readerName = "kinesis-reader-test"
                    checkpointTableName = "system-dev"
                    recordHandler = { id, records ->
                        records.printSimple()
                    }
                }

                // Consumer 시작 (블로킹)
                consumer.start()

            }
        }
    }
}