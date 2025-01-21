package net.kotlinx.aws.lambdaCommon.handler.s3

import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.json.gson.GsonData
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
                println(GsonData.fromObj(input))

                val out = S3LogicInput.parseJson(json)
                check(out.logicId == input.logicId)

            }
        }
    }
}