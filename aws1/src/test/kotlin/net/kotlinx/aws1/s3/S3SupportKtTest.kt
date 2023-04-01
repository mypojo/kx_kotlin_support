package net.kotlinx.aws1.s3

import aws.sdk.kotlin.services.s3.listBuckets
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.toAwsClient1
import org.junit.jupiter.api.Test

internal class S3SupportKtTest {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @Test
    fun `기본테스트`() {


        runBlocking {

            while (true) {
                println(aws.s3.listBuckets {}.buckets!!.map { it.name })
            }


        }


    }

}