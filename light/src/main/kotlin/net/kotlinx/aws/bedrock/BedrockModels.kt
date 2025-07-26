package net.kotlinx.aws.bedrock

import net.kotlinx.ai.AiModel

/**
 * 모델 & 비용 참고
 * https://aws.amazon.com/ko/bedrock/pricing/
 * https://docs.aws.amazon.com/bedrock/latest/userguide/models-supported.html
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

    /**
     * 한국서버 기준 - 크로스 리전으로 우회
     * https://ap-northeast-2.console.aws.amazon.com/bedrock/home?region=ap-northeast-2#/inference-profiles
     *  */
    object CrossRegion {

        /** 안됨.. */
        val CLAUDE_4_SONNET = AiModel {
            id = "apac.anthropic.claude-sonnet-4-20250514-v1:0"
            name = "클로드4소넷"
            costOfInputToken = 0.003
            costOfOutputToken = 0.015
        }

        val NOVA_PRO = AiModel {
            id = "apac.amazon.nova-pro-v1:0"
            name = "노바_프로"
            costOfInputToken = 0.00095
            costOfOutputToken = 0.0002375
        }

        val NOVA_LITE = AiModel {
            id = "apac.amazon.nova-lite-v1:0"
            name = "노바_라이트"
            costOfInputToken = 0.000071
            costOfOutputToken = 0.00001775
        }

        val NOVA_MICRO = AiModel {
            id = "apac.amazon.nova-micro-v1:0"
            name = "노바_마이크로"
            costOfInputToken = 0.000041
            costOfOutputToken = 0.00001025
        }

    }


}