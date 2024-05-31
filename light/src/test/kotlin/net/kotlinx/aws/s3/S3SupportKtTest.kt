package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.listBuckets
import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print

internal class S3SupportKtTest : BeSpecLight() {

    private val profileName by lazy { findProfile28() }
    private val aws by lazy { koin<AwsClient1>(profileName) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("S3SupportKt") {

            Then("버킷 리스팅") {
                val buckets = aws.s3.listBuckets {}.buckets!!
                buckets.size shouldBeGreaterThan 0
                buckets!!.print()
            }

            Then("페이징읽기") {
                val files = aws.s3.listFiles("$profileName-work-dev", "code/")
                files.size shouldBeGreaterThan 0
                files.print()
            }

            Then("디렉토링") {
                val files = aws.s3.listDirs("$profileName-work-dev", "collect/")
                files.size shouldBeGreaterThan 0
                files.print()
            }
        }
    }

}