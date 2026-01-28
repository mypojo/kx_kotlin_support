package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.model.HeadObjectResponse
import aws.smithy.kotlin.runtime.time.Instant
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import net.kotlinx.kotest.modules.BeSpecLight
import java.time.LocalDate

class S3GetHeadObjectResponseSupportTest : BeSpecLight() {
    init {
        Given("HeadObjectResponse 확장 함수 테스트") {
            val response = HeadObjectResponse {
                acceptRanges = "bytes"
                contentLength = 37805L
                contentType = "binary/octet-stream"
                eTag = "\"1b9e1aef16cebf8c304dca20bb923901\""
                lastModified = Instant.fromIso8601("2026-01-27T09:24:01Z")
                metadata = mapOf("test-key" to "test-value")
            }

            Then("확장 속성들이 올바르게 추출되어야 함") {
                response.size shouldBe 37805L
                response.contentTypeName shouldBe "binary/octet-stream"
                response.lastModifiedDate shouldBe LocalDate.of(2026, 1, 27)
            }

            Then("toSummaryMap 은 null 이 아닌 값만 포함해야 함") {
                val summaryMap = response.toSummaryMap()
                summaryMap["acceptRanges"] shouldBe "bytes"
                summaryMap["contentLength"] shouldBe "37805"
                summaryMap.containsKey("archiveStatus") shouldBe false
            }

            Then("toSummaryString 은 올바른 포맷이어야 함") {
                val summaryString = response.toSummaryString()
                summaryString shouldContain "HeadObjectResponse("
                summaryString shouldContain "acceptRanges=bytes"
                summaryString shouldContain "contentLength=37805"
                summaryString shouldNotContain "archiveStatus=null"
            }
        }
    }
}
