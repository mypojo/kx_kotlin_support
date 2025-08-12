package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.deleteObject
import aws.sdk.kotlin.services.s3.listBuckets
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

internal class CsvS3MultipartUploadCollectorTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    companion object {
        private val log = KotlinLogging.logger {}
    }

    init {
        initTest(KotestUtil.PROJECT)

        Given("CsvS3MultipartUploadCollector") {
            Then("2MB 기준 멀티파트 업로드 -> 다운로드 검증 -> 삭제") {
                val bucket = "${findProfile97}-work-dev"
                val keyPrefix = "upload/csv_multi_test/"
                val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
                val key = "$keyPrefix$ts.csv"

                // 버킷 존재 여부 확인 (없으면 스킵)
                val exists = aws.s3.listBuckets {}.buckets?.any { it.name == bucket } == true
                if (!exists) {
                    log.warn { "테스트 버킷이 존재하지 않아 스킵합니다. bucket=$bucket" }
                    return@Then
                }

                val header = listOf("keyword", "adCost", "payload")

                // 업로드할 데이터 구성: 3000행 * 3회, 행당 ~700B payload로 2MB 임계 유도
                var expectedCount = 0L
                var expectedSum = 0L

                val collector = CsvS3MultipartUploadCollector {
                    s3 = aws.s3
                    this.bucket = bucket
                    this.key = key
                    splitMb = 2 // 2MB 기준으로 파트 분할
                    this.header = header
                }

                fun randomRow(): List<String> {
                    val keyword = buildString {
                        repeat(12) { append(('a'..'z').random()) }
                    }
                    val adCost = Random.nextInt(0, 10_000)
                    val payload = buildString {
                        // 약 680자 + 키워드로 행 크기 확보
                        repeat(680) { append('x') }
                        append('-')
                        append(keyword)
                    }
                    expectedCount += 1
                    expectedSum += adCost
                    return listOf(keyword, adCost.toString(), payload)
                }

                // 3번 청크로 emit -> 대략 3파트 이상 업로드 예상
                repeat(3) {
                    val chunk = List(3000) { randomRow() }
                    collector.emit(chunk)
                }

                collector.close()

                // 다운로드 및 검증 (헤더 스킵)
                var actualCount = 0L
                var actualSum = 0L
                var first = true
                aws.s3.getObjectCsvFlow(bucket, key) { flow ->
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

                // 정리: 삭제 후 확인
                aws.s3.deleteObject {
                    this.bucket = bucket
                    this.key = key
                }
                val metadata = aws.s3.getObjectMetadata(bucket, key)
                metadata shouldBe null
            }
        }
    }
}
