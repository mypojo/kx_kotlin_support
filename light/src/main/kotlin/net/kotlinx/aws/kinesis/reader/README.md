# Kinesis Reader

이 패키지는 AWS Kinesis 스트림에서 데이터를 읽고 쓰기 위한 기능을 제공합니다.

## 주요 기능

- Kinesis 스트림에 레코드 쓰기 (단일 및 배치)
- Kinesis 스트림에서 레코드 읽기
- DynamoDB를 사용한 체크포인트 관리
- 샤드 관리 및 모니터링

## 사용 방법

### 프로듀서 (Producer)

Kinesis 스트림에 데이터를 쓰기 위해 `KinesisProducer` 클래스를 사용합니다.

```kotlin
// 프로듀서 생성
val producer = KinesisProducer()

try {
    // 단일 레코드 전송
    val sequenceNumber = producer.putRecord(
        streamName = "my-stream",
        data = """{"event": "user_login", "userId": "user123"}""",
        partitionKey = "user123"
    )
    
    // 배치 레코드 전송
    val records = listOf(
        """{"event": "page_view", "userId": "user1"}""" to "user1",
        """{"event": "page_view", "userId": "user2"}""" to "user2"
    )
    val successCount = producer.putRecords("my-stream", records)
} finally {
    producer.close() // 리소스 정리
}
```

### 컨슈머 (Consumer)

Kinesis 스트림에서 데이터를 읽기 위해 `KinesisConsumerManager` 클래스를 사용합니다.

```kotlin
// 컨슈머 생성
val consumer = KinesisConsumerManager(
    streamName = "my-stream",
    applicationName = "my-app",
    recordHandler = { record ->
        // 레코드 처리 로직
        println("수신된 레코드:")
        println("  샤드: ${record.shardId}")
        println("  파티션키: ${record.partitionKey}")
        println("  데이터: ${record.data}")
        println("  시퀀스번호: ${record.sequenceNumber}")
        
        // 여기에 실제 비즈니스 로직 구현
        // 예: 데이터베이스 저장, 다른 서비스 호출, 메시지 발행 등
    }
)

// 컨슈머 시작 (블로킹)
consumer.start()

// 필요시 중지
// consumer.stop()
```

## 주요 클래스

### KinesisProducer

Kinesis 스트림에 레코드를 전송하는 프로듀서 클래스입니다.

- `putRecord`: 단일 레코드를 Kinesis 스트림에 전송
- `putRecords`: 여러 레코드를 Kinesis 스트림에 배치로 전송
- `close`: 클라이언트 연결 종료

### KinesisConsumerManager

Kinesis 스트림의 샤드를 관리하고 레코드를 처리하는 매니저 클래스입니다.

- `start`: 컨슈머 매니저 시작
- `stop`: 컨슈머 매니저 중지

### ShardProcessor

Kinesis 스트림의 특정 샤드에서 레코드를 읽고 처리하는 클래스입니다.

### CheckpointManager

Kinesis 스트림의 샤드 처리 위치를 DynamoDB에 저장하고 관리하는 클래스입니다.

### 데이터 클래스

- `KinesisRecord`: Kinesis 레코드 데이터 클래스
- `CheckpointData`: 체크포인트 데이터 클래스

## 예제

전체 예제는 `KinesisExample.kt` 파일을 참조하세요.