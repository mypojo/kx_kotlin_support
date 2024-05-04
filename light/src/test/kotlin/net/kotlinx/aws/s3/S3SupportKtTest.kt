package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.listBuckets
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient1
import net.kotlinx.core.time.measureTimeString
import net.kotlinx.kotest.BeSpecLight
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import java.io.File

internal class S3SupportKtTest : BeSpecLight() {

    val aws: AwsClient1 by inject()

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

    @Test
    fun `업로드속도측정`() {
        runBlocking {

            measureTimeString {
                val suff = "dev"
                val zipFile = File("D:\\DATA\\WORK\\allDependencies.zip")
                println("###### 레이어 파일(${zipFile.length() / 1024 / 1024}mb)을 업로드합니다... ")
                val layerName = "xx-layer_common-$suff"
                //aws.s3.putObject("xx-work-$suff", "code/${layerName}/${zipFile.name}", zipFile)
            }.also {
                println(it)
            }

        }

    }
}