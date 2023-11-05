package net.kotlinx.aws.sfn

import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class SfnSupportKtTest : TestRoot() {

    val AWS = AwsConfig("skale").toAwsClient1()

    @Test
    fun test() {
        runBlocking {
            val lists = AWS.sfn.listExecutions("289023186990", "skale-batchStep-dev", ExecutionStatus.Succeeded)
            lists.executions!!.forEach {
                println(it)
            }

            lists.executions!!.first().executionArn

            val desc = AWS.sfn.describeExecution(lists.executions!!.first().executionArn!!)
            println(desc)
            println(desc.input)
            println(desc.inputDetails)

        }
    }

}