# Kotlin 코딩 가이드라인

## 기본방침
1. 처리 과정 설명이나 답변은 가능하면 한글을 사용해줘
2. 처리가 완료되면 슬렉으로 간단하게 완료 메세지를 전달해줘
   1. 채널 이름 = 'ai-콜백'

## gradle 멀티프로젝트 구조
구조는 의존성의 크기과 관계가 있음
core : 최소한의 의존성을 가짐
light : AWS Lambda 를 위한 의존성을 가짐

## 로깅
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

## 클래스파일 생성
가능하면 클래스당 1개의 파일을 생성해줘
확장 함수 기능을 추가할때는 xxxSupport.kt 이런식으로 접미어가 담긴 파일을 만든 후, 거기에 추가해줘

