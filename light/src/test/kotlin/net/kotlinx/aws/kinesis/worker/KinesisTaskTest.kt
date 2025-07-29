package net.kotlinx.aws.kinesis.worker

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.json.gson.json
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import kotlin.time.Duration.Companion.seconds

/**
 * KinesisTask 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. A 파티션에 데이터 100개 넣기
 * 2. B 파티션에서 응답 100개 받기
 * 3. 모든 응답을 받으면 완료 처리
 */
class KinesisTaskTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("Kinesis 태스크") {
            
            val streamName = "${findProfile49}-task-dev"
            val taskName = "testTask"
            val taskId = "12345"
            
            When("태스크 설정 및 시작") {
                
                // 워커 설정 (in 타입 데이터를 처리하고 out 타입으로 전송하는 역할)
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
                
                // 태스크 설정
                val task = KinesisTask {
                    aws = aws49
                    this.streamName = streamName
                    this.taskName = taskName
                    this.taskId = taskId
                    // readerName은 내부적으로 "task-{taskId}"로 설정됨
                    checkpointTableName = "system-dev"
                    dataGenerator = { index: Int ->
                        json {
                            "id" to index
                            "message" to "Test message $index"
                            "timestamp" to System.currentTimeMillis()
                        }
                    }
                    onComplete = {
                        log.info { "태스크가 성공적으로 완료되었습니다!" }
                    }
                }
                
                Then("태스크가 정상적으로 시작된다") {
                    runBlocking {
                        // 워커 시작 (in 타입 데이터를 처리하고 out 타입으로 전송하는 역할)
                        worker.start()
                        
                        // 잠시 대기 (워커가 시작되도록)
                        delay(5.seconds)
                        
                        // 테스트 데이터 생성 (100개)
                        val testData = List(100) { index -> task.dataGenerator(index) }
                        
                        // 태스크 실행 (데이터 입력 및 응답 대기)
                        // execute 메서드는 모든 응답이 도착할 때까지 블로킹됨
                        val result = task.execute(kotlinx.coroutines.flow.flowOf(testData))
                        
                        log.info { "태스크 실행 결과: $result 개의 응답 수신" }
                        
                        // 워커 종료
                        worker.stop()
                    }
                }
            }
        }
    }
    
    companion object {
        private val log = KotlinLogging.logger {}
    }
}