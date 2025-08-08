package net.kotlinx.aws.sqs.worker

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kotlinx.concurrent.delay
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.time.TimeStart
import net.kotlinx.time.toKr01
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay as coroutineDelay

/**
 * SqsWorker 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. 단일 워커 시작 테스트
 * 2. 여러 워커 병렬 실행 테스트
 */
class SqsWorker_워커테스트 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("SQS 워커") {

            val requestQueueUrl = "${findProfile49}-request-dev"
            val resultQueueUrl = "${findProfile49}-result-dev"
            val process: suspend (List<SqsTaskRecord>) -> Unit = { records ->
                val start = TimeStart()
                records.forEach {
                    it.result.put("processed", true)
                    it.result.put("time", LocalDateTime.now().toKr01())
                    log.trace { " -> ${it.result}" }
                    500.milliseconds.delay()
                }
                log.info { "워커 테스트: ${records.size}개의 메시지 처리 -> $start" }
            }

            When("워커 시작 - 단일 워커") {
                val worker = SqsWorker {
                    aws = aws49
                    this.requestQueueUrl = requestQueueUrl
                    this.resultQueueUrl = resultQueueUrl
                    workerName = "worker-single"
                    handler = process
                    maxNumberOfMessages = 4  // 일부러 작게 설정
                }
                runBlocking {
                    // 별도 코루틴에서 워커 시작
                    val job = GlobalScope.launch {
                        worker.start()
                    }
                    
                    // 5초 후 워커 중지
                    coroutineDelay(5000)
                    worker.stop()
                    
                    // 워커 종료 대기
                    job.join()
                }
                // 처리 결과 로그 확인
            }

            When("워커 시작 - 여러 워커 병렬 실행") {
                runBlocking {
                    val jobs = (0 until 2).map { serverIndex ->
                        GlobalScope.launch {
                            val worker = SqsWorker {
                                aws = aws49
                                this.requestQueueUrl = requestQueueUrl
                                this.resultQueueUrl = resultQueueUrl
                                workerName = "worker-${serverIndex.toString().padStart(2, '0')}"
                                handler = process
                                maxNumberOfMessages = 4  // 일부러 작게 설정
                            }
                            
                            // 워커 시작
                            val workerJob = GlobalScope.launch {
                                worker.start()
                            }
                            
                            // 5초 후 워커 중지
                            coroutineDelay(5000)
                            worker.stop()
                            
                            // 워커 종료 대기
                            workerJob.join()
                        }
                    }
                    
                    // 모든 작업 완료 대기
                    jobs.forEach { it.join() }
                }
                // 병렬 처리 결과 로그 확인
            }

            When("워커 시작 및 중지 테스트") {
                val worker = SqsWorker {
                    aws = aws49
                    this.requestQueueUrl = requestQueueUrl
                    this.resultQueueUrl = resultQueueUrl
                    workerName = "worker-stop-test"
                    handler = process
                    stopCallback = { w ->
                        log.info { "워커 ${w.workerName} 종료 콜백 실행" }
                    }
                }
                
                // 워커 시작 후 잠시 실행
                runBlocking {
                    // 별도 코루틴에서 워커 시작
                    val job = GlobalScope.launch {
                        worker.start()
                    }
                    
                    // 5초 후 워커 중지
                    coroutineDelay(5000)
                    worker.stop()
                    
                    // 워커 종료 대기
                    job.join()
                }
                
                log.info { "워커 시작 및 중지 테스트 완료" }
            }
        }
    }
}