package net.kotlinx.openAi

import com.aallam.openai.client.OpenAIHost
import net.kotlinx.ai.AiModel


/**
 * 각 API 모델들
 * 보통 AI 벤더들은 요금제가 2개이다
 * ex) GPT, Perplexity
 *
 * #1 웹 콘솔 = 월 과금
 * #2 API 호출 = 크레딧 충전해서 사용하는 종량제 => 이걸로 해야함
 * */
object OpenAiModels {

    /**
     * 중국 딥시크
     * https://api-docs.deepseek.com/quick_start/pricing/
     *
     * 아직 이미지 지원 안함
     *  */
    object Deepseek {

        val HOST = OpenAIHost("https://api.deepseek.com")

        /** 애네 이거 원툴임 */
        val CHAT: AiModel = AiModel {
            id = "deepseek-chat"
            name = "딥시크"
            costOfInputToken = 0.14 / 1000
            costOfOutputToken = 0.28 / 1000
        }

    }

    /**
     * https://platform.openai.com/account/limits
     * 2024년 2월 기준
     * #1. batch는 채팅만됨
     * #2. 채팅은 어시스턴트, 파일업로드 등 지원안함
     *
     * 가격표 & 최종학습일
     * https://openai.com/api/pricing/
     * */
    object Gpt {

        //==================================================== 채팅 ======================================================

        /**
         * 코딩 / 수학 / 과학
         * 토큰 1M당 3$
         * */
        const val O1_MINI: String = "o1-mini-2024-09-12"

        //==================================================== 어시스턴트 가능 ======================================================

        /** GPT */
        val GPT_4O: AiModel = AiModel {
            id = "gpt-4o-2024-11-20"
            name = "gpt-4o"
            costOfInputToken = 2.50 / 1000
            costOfOutputToken = 10.0 / 1000
        }

        /** GPT 미니 */
        val GPT_4O_MINI: AiModel = AiModel {
            id = "gpt-4o-mini-2024-07-18"
            name = "gpt-4o-mini"
            costOfInputToken = 0.150 / 1000
            costOfOutputToken = 0.6 / 1000
        }
    }

    /**
     * https://docs.perplexity.ai/guides/pricing
     * API 사용시 GPT 처럼 크레딧을 충전해서 사용해야 한다
     *
     * 2024년 2월 기준
     * #채팅만 지원함
     *
     * Perplexity 는 시스템 -> 유저 -> 어시스턴트 -> 유저 -> 어시스턴트 ... 반복   으로 구성되어야 한다
     *  */
    object Perplexity {

        val HOST = OpenAIHost("https://api.perplexity.ai")

        val SONAR01: AiModel = AiModel {
            id = "sonar"
            name = "퍼플렉시티01"
            costOfRequest = 5.0 / 1000
            costOfInputToken = 1.0 / 1000
            costOfOutputToken = 1.0 / 1000
        }
        val SONAR02: AiModel = AiModel {
            id = "sonar-reasoning"
            name = "퍼플렉시티02"
            costOfRequest = 5.0 / 1000
            costOfInputToken = 1.0 / 1000
            costOfOutputToken = 5.0 / 1000
        }
        val SONAR03: AiModel = AiModel {
            id = "sonar-reasoning-pro"
            name = "퍼플렉시티03"
            costOfRequest = 5.0 / 1000
            costOfInputToken = 2.0 / 1000
            costOfOutputToken = 8.0 / 1000
        }
        val SONAR04: AiModel = AiModel {
            id = "sonar-pro"
            name = "퍼플렉시티04"
            costOfRequest = 5.0 / 1000
            costOfInputToken = 3.0 / 1000
            costOfOutputToken = 15.0 / 1000
        }


    }

}