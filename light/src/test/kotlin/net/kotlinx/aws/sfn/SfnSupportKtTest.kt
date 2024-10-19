package net.kotlinx.aws.sfn

import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print

class SfnSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile28) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("SfnSupportKt") {
            Then("SFN 리스팅 & 상세조회") {

                val lists = aws.sfn.listExecutions("${findProfile28}-batchStep-dev", ExecutionStatus.Succeeded)
                lists.executions.size shouldBeGreaterThan 0
                lists.executions.take(10).print()

                val target = lists.executions.first()
                val desc = aws.sfn.describeExecution(target.executionArn)
                log.debug { "로드된 desc : $desc" }
            }
        }
    }

}