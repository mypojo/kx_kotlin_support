package net.kotlinx.aws.athena

import net.kotlinx.test.TestRoot

internal class AthenaS3PartitionModuleTest : TestRoot() {

//    val aws = AwsConfig(profileName = "sin").toAwsClient()
//
//    init {
//        LogBackUtil.logLevelTo("net.kotlinx.aws.athena", Level.TRACE)
//    }
//
//    @Test
//    fun `기본테스트`() {
//        val partitionModule = AthenaS3PartitionModule(
//            aws.s3,
//            AthenaModule(aws, "wp", "workgroup-dev"),
//            "sin-data-dev",
//            "data",
//            "http_log",
//            listOf("basic_date", "name")
//        )
//        runBlocking {
//            //partitionModule.listAndUpdate("20230120","demo")
//            //partitionModule.listAndUpdate("20230120")
//            partitionModule.listAndUpdate()
//        }
//    }
//
//    @Test
//    fun `event1_web`() {
//        val partitionModule = AthenaS3PartitionModule(
//            aws.s3,
//            AthenaModule(aws, "wd", "workgroup-dev"),
//            "sin-work-dev",
//            "collect",
//            "event1_web",
//            listOf("basicDate", "hh")
//        )
//        runBlocking { partitionModule.listAndUpdate() }
//    }
//
//    @Test
//    fun `http_log`() {
//        val partitionModule = AthenaS3PartitionModule(
//            aws.s3,
//            AthenaModule(aws, "wd", "workgroup-dev"),
//            "sin-data-dev",
//            "data",
//            "http_log",
//            listOf("basicDate", "name")
//        )
//        runBlocking { partitionModule.listAndUpdate() }
//    }

}