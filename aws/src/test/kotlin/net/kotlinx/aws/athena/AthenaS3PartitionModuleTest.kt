package net.kotlinx.aws.athena

import ch.qos.logback.classic.Level
import io.kotest.common.runBlocking
import net.kotlinx.TestRoot
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core2.logback.LogBackUtil
import org.junit.jupiter.api.Test

internal class AthenaS3PartitionModuleTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun `기본테스트`() {

        LogBackUtil.logLevelTo("net.kotlinx.aws.athena", Level.TRACE)

        val partitionModule = AthenaS3PartitionModule(
            aws.s3,
            AthenaModule(aws, "wd", "workgroup-dev"),
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

    @Test
    fun `맵더하기`() {

        val a = mapOf("a" to 1, "c" to 3)
        val b = mapOf("b" to 2)

        println(a + b)
        println(a + ("d" to 33))
        println(a + b + ("c" to 33))

    }

}