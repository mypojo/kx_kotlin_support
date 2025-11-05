package net.kotlinx.aws.bedrock

import net.kotlinx.kotest.modules.BeSpecLight


class BedrockAgentSupportKtTest : BeSpecLight() {

    init {
        Given("updateAgentInstruction 동작 검증") {
            Then("agentId 와 instruction 이 UpdateAgentRequest 빌더에 정상 반영된다") {
                //given

                //aws49.bra.updateAgentInstruction("7V7ABTRDTY", "{}")

            }
        }
    }
}
