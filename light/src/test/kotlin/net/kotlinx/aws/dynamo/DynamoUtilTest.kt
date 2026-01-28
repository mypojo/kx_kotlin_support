package net.kotlinx.aws.dynamo

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.time.LocalDateTime
import java.time.ZoneOffset

class DynamoUtilTest : BeSpecHeavy() {

    init {
        Given("DynamoUtil TTL 테스트") {
            val dateTime = LocalDateTime.of(2026, 1, 27, 15, 0, 0)

            Then("LocalDateTime 기반 TTL은 Epoch Second여야 한다") {
                val ttl = DynamoUtil.ttl(dateTime, ZoneOffset.UTC)
                ttl shouldBe dateTime.toEpochSecond(ZoneOffset.UTC)
                ttl shouldBe 1769526000L
            }
        }
    }
}
