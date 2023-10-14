//package net.kotlinx.aws.athena
//
//import net.kotlinx.aws.AwsConfig
//import net.kotlinx.aws.module.batchStep.BatchStepTable
//import net.kotlinx.aws.toAwsClient
//import net.kotlinx.core.test.TestRoot
//import org.junit.jupiter.api.Test
//
//class AthenaTableTest : TestRoot() {
//
//    val aws = AwsConfig(profileName = "sin").toAwsClient()
//    val athenaModule = AthenaModule(aws, workGroup = "workgroup-prod", database = "d")
//
//    @Test
//    fun create() {
//        val demo = BatchStepTable.result.apply {
//            tableName = "demo"
//            location = "s3://demo-work-dev/upload/sfnBatchModuleOutput/"
//        }
//        log.info { "테이블이 생성됩니다\n${demo.create()}" }
////        athenaModule.execute(demo.drop())
////        athenaModule.execute(demo.create())
//    }
//
//
//    @Test
//    fun `복잡한거`() {
//        //나중에 이벤트로그 할때 이거 옮기기
//        val demo = AthenaTable {
//            tableName = "demo"
//            location = "s3://sin-work-dev/collect/event1_job/"
//            schema = mapOf(
//                "detail-type" to "string",
//                "account" to "string",
//                "detail" to mapOf(
//                    "eventId" to "bigint",
//                    "eventDate" to "string",
//                    "datas" to listOf(
//                        "id" to "string",
//                        "x" to "string",
//                    ),
//                ),
//            )
//            partition = mapOf(
//                "basicDate" to "string",
//                "hh" to "string",
//            )
//            athenaTableFormat = AthenaTableFormat.Json
//            athenaTablePartitionType = AthenaTablePartitionType.Index
//        }
//        println(demo.drop())
//        println(demo.create())
//    }
//
//}