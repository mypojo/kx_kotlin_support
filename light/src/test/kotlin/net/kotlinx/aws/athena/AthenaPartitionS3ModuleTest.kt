package net.kotlinx.aws.athena

import ch.qos.logback.classic.Level
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.logback.LogBackUtil
import net.kotlinx.system.DeploymentType

/** 권장하지 않음 */
internal class AthenaPartitionS3ModuleTest : BeSpecHeavy() {

    private val profileName by lazy { findProfile97 }
    private val subName by lazy { findProfile99 }

    init {
        initTest(KotestUtil.IGNORE)

        Given("AthenaS3PartitionModule") {

            val awsClient = Koins.koin<AwsClient>(profileName)
            val deploymentType = DeploymentType.DEV

            val athena = AthenaModule {
                aws = awsClient
                database = "${subName}-${deploymentType.suff}"
                workGroup = "workgroup-${deploymentType.suff}"
            }

            LogBackUtil.logLevelTo(AthenaPartitionS3Module::class.qualifiedName!!, Level.TRACE)

            Then("기본테스트") {
                val partitionModule = AthenaPartitionS3Module {
                    athenaModule = athena
                    tableName = "nv_camp_data"
                    partitionKeys = listOf("basic_date")
                    partitionSqlBuilder = AthenaPartitionSqlBuilder {
                        bucketName = "nabus1-prod"
                        prefix = "data/${findProfile97.uppercase()}"
                    }
                }
                partitionModule.listAndUpdate()
            }


//            Then("event1_web") {
//                val partitionModule = AthenaPartitionS3Module(
//                    aws.s3,
//                    athenaModule,
//                    "sin-work-dev",
//                    "collect",
//                    "event1_web",
//                    listOf("basicDate", "hh")
//                )
//                runBlocking { partitionModule.listAndUpdate() }
//            }
//            Then("http_log") {
//                val partitionModule = AthenaPartitionS3Module(
//                    aws.s3,
//                    athenaModule,
//                    "sin-data-dev",
//                    "data",
//                    "http_log",
//                    listOf("basicDate", "name")
//                )
//                runBlocking { partitionModule.listAndUpdate() }
//            }
        }
    }
}