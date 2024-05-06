package net.kotlinx.aws.lambda

import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicHandler
import net.kotlinx.aws.module.batchStep.stepDefault.StepStartContext
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.time.LocalDateTime

class LambdaHandlerUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("LambdaHandlerUtil") {

            Then("정상 변환") {
                val context = StepStartContext(
                    LocalDateTime.now(),
                    33,
                    1,
                    listOf(obj {
                        S3LogicHandler.KEY to "/aa/bb"
                    }.toString())
                )

                val lambdaMap = LambdaHandlerUtil.anyToLambdaMap(context)
                lambdaMap["total"]!!.toString() shouldBe contain("33")
            }
        }
    }

}