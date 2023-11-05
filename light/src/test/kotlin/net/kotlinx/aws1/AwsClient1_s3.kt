package net.kotlinx.aws1

import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.getObjectLines
import net.kotlinx.aws.s3.listDirs
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.concurrent.collectToList
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

internal class AwsClient1_s3 : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @Test
    fun `프리사인_다운로드`() = runBlocking {
        S3Data.parse("s3://sin-autobid/athena/3eefab99-5ca7-447f-80c8-93ab1860e25a.csv").let {
//            val url = aws.s3.getObjectPresign(it.bucket, it.key)
//            println("프리사인 다운로드 url = $url")
        }
    }

    @Test
    fun `프리사인_업로드`() = runBlocking {
        S3Data.parse("s3://sin-autobid/athena/3eefab99-5ca7-447f-80c8-93ab1860e25a.csv").let {
//            val url = aws.s3.putObjectPresign(it.bucket, it.key)
//            println("프리사인 업로드 url = $url")
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

    @Test
    fun `남은시간`() {
        val paginated = aws.s3.listObjectsV2Paginated {
            this.bucket = "sin-work-dev"
            this.prefix = "upload/sfnBatchModuleOutput/"
        }
        //[1000, 1000, 505]
        runBlocking {
            var remainCnt = 0
            repeat(100) {
                log.info { "데이터 로드.." }
                val remainCnt = paginated.collectToList { it.contents?.size ?: 0 }.sum()
                log.info { "데이터 로드.. $remainCnt" }
                delay(10.seconds)
            }
        }
    }

}
