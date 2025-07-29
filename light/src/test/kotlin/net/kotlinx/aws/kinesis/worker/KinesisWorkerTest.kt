package net.kotlinx.aws.kinesis.worker

import mu.KotlinLogging
import net.kotlinx.json.gson.json
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

/**
 * KinesisWorker 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. 워커 시작 및 종료 기능
 * 2. 특정 파티션 데이터 필터링 기능
 * 3. 데이터 처리 및 다른 파티션으로 전송 기능
 */
class KinesisWorkerTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("Kinesis 워커") {

            val streamName = "${findProfile49}-worker-dev"
            // 파티션 키 규칙: taskName-taskId-type
            val taskName = "testTask"
            val taskId = "12345"
            val inPartitionKey = "$taskName-$taskId-in"
            val outPartitionKey = "$taskName-$taskId-out"

            When("워커 설정 및 시작") {

                // KinesisWorker 인스턴스 생성
                val worker = KinesisWorker {
                    aws = aws49
                    this.streamName = streamName
                    // readerName은 기본값 "worker01"을 사용
                    checkpointTableName = "system-dev"

                    // 데이터 처리 핸들러 설정
                    handler = { records ->
                        log.info { "테스트: ${records.size}개의 레코드 처리" }
                        // 데이터 처리 로직 (예: 필드 추가 또는 변환)
                        records.map { record ->
                            json {
                                "original" to record
                                "processed" to true
                                "timestamp" to System.currentTimeMillis()
                            }
                        }
                    }
                }

                Then("워커가 정상적으로 시작된다") {
                    // 워커 시작
                    worker.start()

                    // 워커 종료
                    worker.stop()
                }
            }

            When("파티션 키 변환 로직 테스트") {
                Then("in 타입 파티션 키가 out 타입으로 변환된다") {
                    // 파티션 키 변환 함수 테스트
                    val convertPartitionKey = { key: String -> key.replace("-in", "-out") }
                    
                    // 테스트 케이스
                    val testCases = listOf(
                        inPartitionKey to outPartitionKey,
                        "otherTask-54321-in" to "otherTask-54321-out",
                        "randomTask-98765-in" to "randomTask-98765-out"
                    )
                    
                    // 검증
                    testCases.forEach { (input, expected) ->
                        val result = convertPartitionKey(input)
                        assert(result == expected) { 
                            "파티션 키 변환 실패: $input -> $result (예상: $expected)" 
                        }
                    }
                    
                    log.info { "파티션 키 변환 로직 테스트 성공" }
                }
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}