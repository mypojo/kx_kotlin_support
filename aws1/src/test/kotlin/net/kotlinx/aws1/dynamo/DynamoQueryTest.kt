package net.kotlinx.aws1.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.TestRoot
import net.kotlinx.aws1.dynamo.DynamoQuery.DynamoQueryKeyEqualTo
import net.kotlinx.aws1.toAwsClient1
import net.kotlinx.core1.time.toLong
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

data class Koo(
    override val pk: String,
    override val sk: String,
    val ttl: Long,
    val demoPageCnt: Long,
) : DynamoData {

    override val tableName: String = "system-dev"

    override fun toAttribute(): Map<String, AttributeValue> = mapOf(
        DynamoDbBasic.pk to AttributeValue.S(pk),
        DynamoDbBasic.sk to AttributeValue.S(sk),
        "ttl" to AttributeValue.N(ttl.toString()),
        "demoPageCnt" to AttributeValue.N(demoPageCnt.toString()),
    )

    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = Koo(
        map[DynamoDbBasic.pk]!!.asS(),
        map[DynamoDbBasic.sk]!!.asS(),
        map["ttl"]!!.asN().toLong(),
        map["demoPageCnt"]!!.asN().toLong(),
    ) as T

}

/** 조회 조건 정의 */
object myQuery : DynamoQueryKeyEqualTo() {
    init {
        limit = 4
    }
}

internal class DynamoQueryTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()
    val client = aws.dynamo

    @Test
    fun 기본테스트() {

        val k1 = Koo("aa", "X553", LocalDateTime.now().plusHours(1).toLong(), 555)
        val k2 = Koo("aa", "c2", LocalDateTime.now().plusHours(1).toLong(), 555)

        val param = k1.copy(sk = "c")

        runBlocking {

            client.putItem(k1)
            client.putItem(k2)

            (0..10).forEach {
                client.putItem(Koo("aa", "num${it}", LocalDateTime.now().plusHours(1).toLong(), it.toLong()))
            }

            val koos = client.query(param, myQuery)
            check(koos.size == 4)

            val koos2 = client.query(param.copy(pk = "xxx"), myQuery)
            check(koos2.isEmpty())

            val koos3 = client.queryAll(param, myQuery)
            check(koos3.size >= 10)



        }

    }

}