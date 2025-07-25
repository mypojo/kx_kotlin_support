package net.kotlinx.aws.kinesis.reader

import kotlinx.coroutines.runBlocking
import java.time.Instant

/**
 * Kinesis 프로듀서 및 컨슈머 사용 예제
 */
object KinesisExample {
    
    /**
     * 예제 실행
     */
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val streamName = "my-kinesis-stream"
        val applicationName = "my-kotlin-consumer"
        
        // Producer 예제
        val producer = KinesisProducer()
        
        try {
            // 테스트 데이터 전송
            repeat(10) { i ->
                producer.putRecord(
                    streamName = streamName,
                    data = """{"userId": "user$i", "action": "test", "timestamp": "${System.currentTimeMillis()}"}""",
                    partitionKey = "user$i"
                )
            }
            
            // Consumer 시작
            val consumer = KinesisReader {
                this.streamName = streamName
                this.applicationName = applicationName
                this.recordHandler = { record ->
                    // 레코드 처리 로직
                    println("수신된 레코드:")
                    println("  샤드: ${record.shardId}")
                    println("  파티션키: ${record.partitionKey}")
                    println("  데이터: ${record.data}")
                    println("  시퀀스번호: ${record.sequenceNumber}")
                    println("  도착시간: ${Instant.ofEpochSecond(record.approximateArrivalTimestamp)}")
                    println("---")
                    
                    // 여기에 실제 비즈니스 로직 구현
                    // 예: 데이터베이스 저장, 다른 서비스 호출, 메시지 발행 등
                }
            }
            
            // Consumer 시작 (블로킹)
            consumer.start()
            
        } catch (e: Exception) {
            println("오류 발생: ${e.message}")
            e.printStackTrace()
        } finally {
            producer.close()
        }
    }
    
    /**
     * 프로듀서만 사용하는 예제
     */
    suspend fun producerExample(streamName: String) {
        val producer = KinesisProducer()
        
        try {
            // 단일 레코드 전송
            val sequenceNumber = producer.putRecord(
                streamName = streamName,
                data = """{"event": "user_login", "userId": "user123", "timestamp": "${System.currentTimeMillis()}"}""",
                partitionKey = "user123"
            )
            println("레코드 전송 완료: $sequenceNumber")
            
            // 배치 레코드 전송
            val records = (1..5).map { i ->
                val data = """{"event": "page_view", "userId": "user$i", "timestamp": "${System.currentTimeMillis()}"}"""
                val partitionKey = "user$i"
                data to partitionKey
            }
            
            val successCount = producer.putRecords(streamName, records)
            println("배치 전송 완료: $successCount/${records.size}")
            
        } finally {
            producer.close()
        }
    }
    
    /**
     * 컨슈머만 사용하는 예제
     */
    suspend fun consumerExample(streamName: String, applicationName: String) {
        val consumer = KinesisReader {
            this.streamName = streamName
            this.applicationName = applicationName
            this.recordHandler = { record ->
                // 간단한 로깅만 수행
                println("레코드 수신: ${record.data}")
            }
        }
        
        // 컨슈머 시작 (블로킹)
        consumer.start()
        
        // 필요시 중지
        // consumer.stop()
    }
}