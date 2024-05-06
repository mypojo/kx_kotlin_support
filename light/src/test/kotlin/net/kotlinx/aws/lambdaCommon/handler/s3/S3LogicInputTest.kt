package net.kotlinx.aws.lambdaCommon.handler.s3

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest


class S3LogicInputTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("S3LogicInput") {
            Then("로직 테스트") {
                val input = S3LogicInput("defaultLogic", listOf("a", "b", "c"))
                val json = input.toJson()
                println(json)
                val out = S3LogicInput.parseJson(json)
                println(out)

                val path = S3LogicPath("upload/sfnBatchModuleInput/16a23f53-15b1-4b48-a628-7708b5b3daee/00000.txt")
                println(path.fileName)
                println(path.pathId)
                println(path.outputDir)
            }
        }
    }
}