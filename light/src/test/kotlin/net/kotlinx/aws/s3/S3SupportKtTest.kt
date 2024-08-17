package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.listBuckets
import ch.qos.logback.classic.Level
import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.aws.AwsClient1
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.logback.LogBackUtil
import net.kotlinx.string.print
import net.kotlinx.system.ResourceHolder

internal class S3SupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient1>(findProfile28) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("S3SupportKt") {

            Then("버킷 리스팅") {
                val buckets = aws.s3.listBuckets {}.buckets!!
                buckets.size shouldBeGreaterThan 0
                buckets!!.print()
            }

            Then("페이징읽기") {
                val files = aws.s3.listFiles("$findProfile28-work-dev", "code/")
                files.size shouldBeGreaterThan 0
                files.print()
            }

            Then("디렉토링") {
                val files = aws.s3.listDirs("$findProfile28-work-dev", "collect/")
                files.size shouldBeGreaterThan 0
                files.print()
            }

            xThen("메타데이터 추가 업로드") {
                val file = ResourceHolder.WORKSPACE.slash("input.csv")
                aws.s3.putObject(
                    "$findProfile28-work-dev", "upload/temp.csv", file, mapOf(
                        "aa" to "bb",
                        "cc" to "dd",
                        "fileName" to "영감님ab12만세"
                    )
                )
            }

            xThen("메타데이터 읽기") {
                val metadata = aws.s3.getObjectMetadata("$findProfile28-work-dev", "upload/temp.csv")!!
                println(metadata)
            }

            xThen("멀티파트 업로드") {
                LogBackUtil.logLevelTo(testClassName, Level.TRACE)
                val file = ResourceHolder.WORKSPACE.slash("aa").slash("bb-202405.csv.zip")
                aws.s3.putObjectMultipart("$findProfile28-work-dev", "upload/temp.csv", file, 100)
            }
        }
    }

}