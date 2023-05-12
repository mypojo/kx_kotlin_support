package net.kotlinx.aws1.s3

import aws.sdk.kotlin.services.s3.listBuckets
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.toAwsClient1
import org.junit.jupiter.api.Test

internal class S3SupportKtTest {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @Test
    fun `페이징읽기`() {
        runBlocking {

            val lines = aws.s3.getObjectLines("sin-work-prod", "job_admin/daily_adspend_job/20230403/20230403_캠페인별_사용_금액.csv")!!
            println(lines.size)
            lines.forEachIndexed { index, strings ->
                println("$index : $strings")
            }

        }
    }


    @Test
    fun `기본테스트`() {
        println("==============")
        runBlocking {
            println(aws.s3.listBuckets {}.buckets!!.map { it.name })
        }
    }

}