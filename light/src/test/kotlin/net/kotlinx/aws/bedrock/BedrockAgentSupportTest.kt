package net.kotlinx.aws.bedrock

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print

class BedrockAgentSupportTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("베드락 에이전트") {

            Then("프롬프트 리스팅") {
                val resp = aws97.bra.listAllPrompts()
                resp.print()
            }

            Then("프롬프트 가져오기") {
                val resp = aws97.bra.getPrompt("WCMF5L3D5Z")
                listOf(resp).print()
            }


        }
    }

}
