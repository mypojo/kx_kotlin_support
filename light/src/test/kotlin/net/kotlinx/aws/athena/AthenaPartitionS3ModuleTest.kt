package net.kotlinx.aws.athena

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

/** 권장하지 않음 */
internal class AthenaPartitionS3ModuleTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("AthenaS3PartitionModule") {

            val aws = Koins.koin<AwsClient1>()

            val athenaModule = AthenaModule{
                //"wp", "workgroup-dev"
            }
            Then("기본테스트") {
                val partitionModule = AthenaPartitionS3Module(
                    aws.s3,
                    athenaModule,
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
                val partitionModule = AthenaPartitionS3Module(
                    aws.s3,
                    athenaModule,
                    "sin-work-dev",
                    "collect",
                    "event1_web",
                    listOf("basicDate", "hh")
                )
                runBlocking { partitionModule.listAndUpdate() }
            }
            Then("http_log") {
                val partitionModule = AthenaPartitionS3Module(
                    aws.s3,
                    athenaModule,
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