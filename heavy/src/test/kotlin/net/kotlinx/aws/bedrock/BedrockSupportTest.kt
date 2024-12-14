package net.kotlinx.aws.bedrock

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class BedrockSupportTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("베드락 코어") {

            Then("모델 리스팅") {
                val resp = aws97.br.listFoundationModels()
                resp.printSimple()
            }

        }
    }

}
