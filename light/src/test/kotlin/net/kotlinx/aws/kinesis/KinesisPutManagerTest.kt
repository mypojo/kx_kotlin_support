package net.kotlinx.aws.kinesis

import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.kinesis.model.PutRecordsResponse
import aws.sdk.kotlin.services.kinesis.model.PutRecordsResultEntry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.kinesis.writer.KinesisWriter
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import kotlin.time.Duration.Companion.milliseconds

/**
 * KinesisPutManager 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. 모든 레코드가 성공적으로 처리되는 경우
 * 2. 일부 레코드가 실패하고 재시도 후 성공하는 경우
 * 3. 일부 레코드가 계속 실패하여 최대 재시도 횟수에 도달하는 경우
 */
class KinesisPutManagerTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("KinesisPutManager") {

            // 모의 AWS 클라이언트 설정
            val mockKinesisClient = mockk<KinesisClient>()
            val mockAwsClient = mockk<AwsClient>()
            every { mockAwsClient.kinesis } returns mockKinesisClient

            // 테스트 데이터 생성 - GsonData 객체 생성
            val testData = listOf(
                GsonData.fromObj(mapOf("id" to "1", "value" to "value1")),
                GsonData.fromObj(mapOf("id" to "2", "value" to "value2")),
                GsonData.fromObj(mapOf("id" to "3", "value" to "value3"))
            )

            // KinesisPutManager 인스턴스 생성
            val manager = KinesisWriter {
                aws = mockAwsClient
                streamName = "test-stream"
                partitionKeyBuilder = { "test-partition" }
                maxRetries = 3
                retryDelay = 10.milliseconds // 테스트에서는 지연 시간을 짧게 설정
            }

            When("모든 레코드가 성공적으로 처리되는 경우") {
                // 모의 응답 설정 - 모든 레코드 성공
                val successResponse = mockk<PutRecordsResponse>()
                every { successResponse.failedRecordCount } returns 0
                every { successResponse.records } returns List(3) {
                    mockk<PutRecordsResultEntry>().apply {
                        every { errorCode } returns null
                    }
                }

                coEvery {
                    mockKinesisClient.putRecords(any())
                } returns successResponse

                Then("모든 레코드가 성공적으로 처리된다") {
                    // 예외 없이 실행되어야 함
                    manager.putRecords(testData)

                    // putRecords가 한 번만 호출되었는지 확인
                    coVerify(exactly = 1) {
                        mockKinesisClient.putRecords(any())
                    }
                }
            }

            When("일부 레코드가 실패하고 재시도 후 성공하는 경우") {
                // 첫 번째 호출에서는 일부 레코드 실패, 두 번째 호출에서는 모두 성공
                val requestSlot = slot<aws.sdk.kotlin.services.kinesis.model.PutRecordsRequest>()
                var callCount = 0

                coEvery {
                    mockKinesisClient.putRecords(capture(requestSlot))
                } answers {
                    callCount++
                    if (callCount == 1) {
                        // 첫 번째 호출: 3개 중 1개 실패
                        mockk<PutRecordsResponse>().apply {
                            every { failedRecordCount } returns 1
                            every { records } returns listOf(
                                mockk<PutRecordsResultEntry>().apply {
                                    every { errorCode } returns null
                                },
                                mockk<PutRecordsResultEntry>().apply {
                                    every { errorCode } returns "ProvisionedThroughputExceededException"
                                },
                                mockk<PutRecordsResultEntry>().apply {
                                    every { errorCode } returns null
                                }
                            )
                        }
                    } else {
                        // 두 번째 호출: 모두 성공
                        mockk<PutRecordsResponse>().apply {
                            every { failedRecordCount } returns 0
                            every { records } returns List(requestSlot.captured.records?.size ?: 0) {
                                mockk<PutRecordsResultEntry>().apply {
                                    every { errorCode } returns null
                                }
                            }
                        }
                    }
                }

                Then("실패한 레코드만 재시도하고 모두 성공한다") {
                    // 예외 없이 실행되어야 함
                    manager.putRecords(testData)

                    // putRecords가 두 번 호출되었는지 확인
                    coVerify(exactly = 2) {
                        mockKinesisClient.putRecords(any())
                    }

                    // 두 번째 호출에서는 실패한 레코드만 재시도했는지 확인
                    requestSlot.captured.records?.size shouldBe 1
                }
            }

            When("일부 레코드가 계속 실패하여 최대 재시도 횟수에 도달하는 경우") {
                // 모든 호출에서 항상 일부 레코드 실패
                coEvery {
                    mockKinesisClient.putRecords(any())
                } returns mockk<PutRecordsResponse>().apply {
                    every { failedRecordCount } returns 1
                    every { records } returns listOf(
                        mockk<PutRecordsResultEntry>().apply {
                            every { errorCode } returns null
                        },
                        mockk<PutRecordsResultEntry>().apply {
                            every { errorCode } returns "InternalFailure"
                        },
                        mockk<PutRecordsResultEntry>().apply {
                            every { errorCode } returns null
                        }
                    )
                }

                Then("최대 재시도 횟수 후 예외가 발생한다") {
                    // IllegalStateException이 발생해야 함
                    val exception = shouldThrow<IllegalStateException> {
                        manager.putRecords(testData)
                    }

                    // 예외 메시지 확인
                    exception.message shouldBe "Failed to put 1 records to Kinesis after 3 retries."

                    // putRecords가 최대 재시도 횟수 + 1번 호출되었는지 확인
                    coVerify(exactly = 4) {
                        mockKinesisClient.putRecords(any())
                    }
                }
            }
        }
    }
}