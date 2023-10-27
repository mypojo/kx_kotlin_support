package net.kotlinx.aws.lambda

import com.lectra.koson.obj
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicHandler
import net.kotlinx.aws.module.batchStep.stepDefault.StepStartContext
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class LambdaHandlerUtilTest : TestRoot() {

    @Test
    fun test() {

        val context = StepStartContext(
            LocalDateTime.now(),
            33,
            1,
            listOf(obj {
                S3LogicHandler.KEY to "/aa/bb"
            }.toString())
        )

        println(LambdaHandlerUtil.anyToLambdaMap(context))


    }

}