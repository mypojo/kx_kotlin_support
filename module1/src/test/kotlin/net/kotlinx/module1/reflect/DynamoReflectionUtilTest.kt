package net.kotlinx.module1.reflect

import io.kotest.common.runBlocking
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.dynamo.DynamoDbBasic
import net.kotlinx.aws1.dynamo.DynamoDbBasicModule
import net.kotlinx.core1.time.toLong
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class DynamoReflectionUtilTest {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun `기본테스트`() {

        data class Koo(
            override val pk: String,
            override val sk: String,
            val expireTime: Long,
            val demoPageCnt: Long,
        ) : DynamoDbBasic

        val k1 = Koo("aa", "c1", LocalDateTime.now().plusHours(1).toLong(), 555)
        val k2 = Koo("aa", "c2", LocalDateTime.now().plusHours(1).toLong(), 555)

        val search = k1.copy(sk = "c")

        val ddb = DynamoDbBasicModule(aws.dynamo, "system-dev", Koo::class)

        runBlocking {

            ddb.putItem(k1)
            ddb.putItem(k2)

            val getData = ddb.getItem(k1)
            check(getData.sk == k1.sk)

            ddb.querykeyEqualTo(search ).forEach {
                check(it.sk.startsWith(search.sk))
            }
            ddb.querySortBeginsWith(search ).forEach {
                check(it.sk.startsWith(search.sk))
            }
        }

    }

}