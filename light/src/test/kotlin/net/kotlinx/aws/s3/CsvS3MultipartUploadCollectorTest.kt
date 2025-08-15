package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.deleteObject
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.flow.collectClose
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

internal class CsvS3MultipartUploadCollectorTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile49) }

    companion object {
        private val log = KotlinLogging.logger {}
    }

    // 공통 테스트 데이터

    private var expectedCount = 0L
    private var expectedSum = 0L

    private fun randomRow(): List<String> {
        val keyword = buildString {
            repeat(12) { append(('a'..'z').random()) }
        }
        val adCost = Random.nextInt(0, 10_000)
        val payload = buildString {
            // 약 1500자로 행 크기 증가 (한 행당 약 1.5KB)
            repeat(1500) { append('x') }
            append('-')
            append(keyword)
        }
        val extraData = buildString {
            // 추가 데이터 필드로 행 크기를 더 늘림 (약 500자 추가)
            repeat(500) { append(('A'..'Z').random()) }
        }
        expectedCount += 1
        expectedSum += adCost
        return listOf(keyword, adCost.toString(), payload, extraData)
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("CsvS3MultipartUploadCollector") {

            val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
            val header = listOf("keyword", "adCost", "payload", "extraData")

            val s3Data = S3Data("${findProfile49}-work-dev", "temp/csv_multi_test/$ts.csv")

            Then("용량 기준 자동 업로드 테스트") {
                expectedCount = 0
                expectedSum = 0
                
                // 대용량 데이터 플로우 생성: 실제 데이터 스트림을 시뮬레이션
                // 한 행당 약 2KB, 총 15,000 라인 = 약 30MB
                val dataFlow = flow {
                    val totalRows = 15000 // 총 15,000 라인 (약 30MB)
                    val batchSize = 1000

                    log.info { "테스트 데이터 생성 시작: ${totalRows} 라인" }
                    for (i in 0 until totalRows step batchSize) {
                        val remainingRows = minOf(batchSize, totalRows - i)
                        val batch = List(remainingRows) { randomRow() }
                        emit(batch)
                        if (i % 5000 == 0) {
                            log.info { "진행률: ${i}/${totalRows} 라인 생성됨" }
                        }
                    }
                    log.info { "테스트 데이터 생성 완료" }
                }

                dataFlow.collectClose {
                    CsvS3MultipartUploadCollector {
                        s3 = aws.s3
                        this.s3Data = s3Data
                        multipartThresholdMb = 7 // 5MB 임계값 (기본값)
                        this.header = header
                    }
                }
            }

            Then("다운로드 및 데이터 검증") {
                var actualCount = 0L
                var actualSum = 0L
                var first = true

                aws.s3.getObjectCsvFlow(s3Data.bucket, s3Data.key) { flow ->
                    flow.collect { row ->
                        if (first) {
                            // 헤더 검증 후 스킵
                            row shouldBe header
                            first = false
                            return@collect
                        }
                        actualCount += 1
                        actualSum += row[1].toLong()
                    }
                }

                actualCount shouldBe expectedCount
                actualSum shouldBe expectedSum
            }

            Then("업로드된 파일 삭제 및 확인") {
                aws.s3.deleteObject {
                    this.bucket = s3Data.bucket
                    this.key = s3Data.key
                }

                val metadata = aws.s3.getObjectMetadata(s3Data.bucket, s3Data.key)
                metadata shouldBe null
            }
        }
    }
}
