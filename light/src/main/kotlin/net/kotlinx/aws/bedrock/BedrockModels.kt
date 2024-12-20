package net.kotlinx.aws.bedrock

import net.kotlinx.ai.AiModel

/**
 * 모델 & 비용 참고
 * https://aws.amazon.com/ko/bedrock/pricing/
 * */
object BedrockModels {

    object OnDemand {
        val CLAUDE_35_SONNET = AiModel {
            id = "anthropic.claude-3-5-sonnet-20240620-v1:0"
            name = "클로드3.5소넷"
            costOfInputToken = 0.003
            costOfOutputToken = 0.015
        }
        val CLAUDE_3_HAIKU = AiModel {
            id = "anthropic.claude-3-haiku-20240307-v1:0"
            name = "클로드3하이쿠"
            costOfInputToken = 0.00025
            costOfOutputToken = 0.00125
        }
    }


}