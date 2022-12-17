package net.kotlinx.aws

import aws.sdk.kotlin.services.lambda.model.ListFunctionsRequest
import aws.sdk.kotlin.services.s3.model.ListBucketsRequest
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.toLocalDateTime
import net.kotlinx.core1.string.toTextGrid
import net.kotlinx.core1.time.toKr01
import org.junit.jupiter.api.Test

internal class AwsClientTest {

    @Test
    fun awsClient() {

        runBlocking {
            val awsConfig = AwsConfig(profileName = "sin")
            val awsClient = awsConfig.toAwsClient()

            awsClient.lambda.listFunctions(ListFunctionsRequest { maxItems = 10 }).functions?.map {
                arrayOf(
                    it.functionName, it.codeSize, it.functionArn
                )
            }?.also {
                listOf("함수명", "코드사이즈", "ARN").toTextGrid(it).print()
            }
            awsClient.s3.listBuckets(ListBucketsRequest { }).buckets?.map {
                arrayOf(
                    it.name, it.creationDate?.toLocalDateTime()?.toKr01()
                )
            }?.also {
                listOf("이름", "생성날짜").toTextGrid(it).print()
            }

        }
    }
}