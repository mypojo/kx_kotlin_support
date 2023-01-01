package net.kotlinx.aws1

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.s3.*
import org.junit.jupiter.api.Test

internal class AwsClient1_s3 {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @Test
    fun `프리사인_다운로드`() = runBlocking {
        S3Data.parse("s3://sin-autobid/athena/3eefab99-5ca7-447f-80c8-93ab1860e25a.csv").let {
            val url = aws.s3.getObjectPresign(it.bucket, it.key)
            println("프리사인 다운로드 url = $url")
        }
    }

    @Test
    fun `프리사인_업로드`() = runBlocking {
        S3Data.parse("s3://sin-autobid/athena/3eefab99-5ca7-447f-80c8-93ab1860e25a.csv").let {
            val url = aws.s3.putObjectPresign(it.bucket, it.key)
            println("프리사인 업로드 url = $url")
        }
    }

    @Test
    fun `CSV읽기`() = runBlocking {
        S3Data.parse("s3://sin-autobid/athena/3eefab99-5ca7-447f-80c8-93ab1860e25a.csv").let {
            val lines = aws.s3.getObjectLines(it.bucket, it.key)
            println(lines)
        }
    }

    @Test
    fun `디렉토링`() = runBlocking {
        aws.s3.listDirs("sin-autobid", "data/autobid_rank/basic_date=20221227/kwd_name=반바지/").forEach { println(it) }
    }

}
