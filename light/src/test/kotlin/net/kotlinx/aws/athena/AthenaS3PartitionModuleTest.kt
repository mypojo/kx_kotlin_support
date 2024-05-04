package net.kotlinx.aws.athena

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class AthenaS3PartitionModuleTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        val aws = Koins.koin<AwsClient1>()

        Given("AthenaS3PartitionModule") {
            Then("기본테스트") {
                val partitionModule = AthenaS3PartitionModule(
                    aws.s3,
                    AthenaModule("wp", "workgroup-dev"),
                    "sin-data-dev",
                    "data",
                    "http_log",
                    listOf("basic_date", "name")
                )
                runBlocking {
                    //partitionModule.listAndUpdate("20230120","demo")
                    //partitionModule.listAndUpdate("20230120")
                    partitionModule.listAndUpdate()
                }
            }
            Then("event1_web") {
                val partitionModule = AthenaS3PartitionModule(
                    aws.s3,
                    AthenaModule("wd", "workgroup-dev"),
                    "sin-work-dev",
                    "collect",
                    "event1_web",
                    listOf("basicDate", "hh")
                )
                runBlocking { partitionModule.listAndUpdate() }
            }
            Then("http_log") {
                val partitionModule = AthenaS3PartitionModule(
                    aws.s3,
                    AthenaModule("wd", "workgroup-dev"),
                    "sin-data-dev",
                    "data",
                    "http_log",
                    listOf("basicDate", "name")
                )
                runBlocking { partitionModule.listAndUpdate() }
            }
        }
    }
}