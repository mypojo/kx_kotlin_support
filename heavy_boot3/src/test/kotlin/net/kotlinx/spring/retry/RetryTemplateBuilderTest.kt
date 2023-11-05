package net.kotlinx.spring.retry

import aws.sdk.kotlin.services.dynamodb.model.DynamoDbException
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.dynamo.updateItemMap
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.concurrent.parallelExecute
import net.kotlinx.test.TestLevel03
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class RetryTemplateBuilderTest : TestRoot() {

    @Test
    fun test() {
        val retry = RetryTemplateBuilder().maxAttempts(3, IllegalStateException::class.java).backoff(1.seconds).build()
        (0..5).forEach { c ->
            log.info { "[$c] 테스팅.. " }
            retry.withRetry {
                val num = Random.nextInt(2)
                log.debug { " -> [$c] 주사위 굴림 $num " }
                if (num == 0) throw IllegalStateException("주사위 실패!!")
            }
        }
    }

    @TestLevel03
    fun `DDB 대량입력 리트라이 테스트`() {

        val aws = AwsConfig(profileName = "sin").toAwsClient1()
        val retry = RetryTemplateBuilder().maxAttempts(3, DynamoDbException::class.java).backoff(1.seconds).build()

        runBlocking {

//            aws.dynamo.updateItem {
//                this.tableName = "system-dev"
//                this.returnValues = ReturnValue.UpdatedNew //새 값 리턴
//                this.key = mapOf(
//                    DynamoDbBasic.pk to AttributeValue.S("p1"),
//                    DynamoDbBasic.sk to AttributeValue.S("s1"),
//                )
//                this.updateExpression = "set results = :val"
//                this.expressionAttributeValues = mapOf(
//                    ":val" to emptyMap<String, String>().toDynamoAttribute()
//                )
//            }

            (0 until 1000).map { no ->
                Callable {
                    retry.withRetry {
                        runBlocking {
                            aws.dynamo.updateItemMap("system-dev", "p1", "s1", "results", mapOf("user${no}" to "$no"))
                        }
                    }
                }
            }.parallelExecute(10)

        }


    }

}