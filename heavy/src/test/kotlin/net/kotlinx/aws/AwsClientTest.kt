package net.kotlinx.aws

import aws.sdk.kotlin.services.lambda.listFunctions
import aws.sdk.kotlin.services.s3.listBuckets
import aws.sdk.kotlin.services.secretsmanager.getSecretValue
import kotlinx.coroutines.runBlocking
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.core.time.toKr01
import org.junit.jupiter.api.Test
import kotlin.concurrent.timerTask

internal class AwsClientTest {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun `일반-조회`() = runBlocking {

        aws.lambda.listFunctions { maxItems = 10 }.functions?.map {
            arrayOf(
                it.functionName, it.codeSize, it.functionArn
            )
        }?.also {
            listOf("함수명", "코드사이즈", "ARN").toTextGrid(it).print()
        }

        aws.s3.listBuckets {}.buckets?.map {
            arrayOf(
                it.name, it.creationDate?.toLocalDateTime()?.toKr01()
            )
        }?.also {
            listOf("이름", "생성날짜").toTextGrid(it).print()
        }

        val secretValue = aws.sm.getSecretValue { this.secretId = "sin-rds_secret-dev" }
        check(secretValue.secretString != null)


    }

    @Test
    fun `일반-조회2`(){


        timerTask {
            println("aaa")



        }

    }


}