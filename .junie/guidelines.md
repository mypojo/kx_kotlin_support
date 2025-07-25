# Kotlin 코딩 가이드라인

gradle 멀티프로젝트 구조는 의존성의 크기과 관계가 있음
core : 최소한의 의존성을 가짐
light : AWS Lambda 를 위한 의존성을 가짐


로그 설정시 아래처럼 companion object /  KotlinLogging 으로 설정해줘
```kotlin
companion object {
    private val log = KotlinLogging.logger {}
}
```
로그 쓰기시 항상 아래처럼 {} 를 사용해줘
```kotlin
log.warn { "Failed to put ${remainingRecords.size} records to Kinesis after $maxRetries retries." }
```

