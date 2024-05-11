package net.kotlinx.aws.sfn

import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsConfig
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print

class SfnSupportKtTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT02)

        Given("SfnSupportKt") {
            val aws = koin<AwsClient1>()
            Then("SFN 리스팅 & 상세조회") {

                val awsConfig = koin<AwsConfig>()
                val lists = aws.sfn.listExecutions("${awsConfig.profileName}-batchStep-dev", ExecutionStatus.Succeeded)
                lists.executions.size shouldBeGreaterThan 0
                lists.executions.take(10).print()

                val target = lists.executions.first()
                val desc = aws.sfn.describeExecution(target.executionArn)
                log.debug { "로드된 desc : $desc" }
            }
        }
    }

}