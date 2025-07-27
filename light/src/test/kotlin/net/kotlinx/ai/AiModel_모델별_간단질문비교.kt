package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

@OptIn(BetaOpenAI::class)
class AiModel_모델별_간단질문비교 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("GPT") {

            val set = AiModelSet {
                aws = aws97
                this.prompt = obj {
                    "codeLanguage" to "kotlin"
                    "awsSdk" to "kotlin SDK"
                    "requirements" to obj {
                        "outputFormat" to "single JSON output"
                        "allowedLanguages" to arr["korean", "english"]
                        "restrictions" to arr[
                            "no markdown",
                            "no emoji",
                            "no lineseparator",
                            "no chinese characters"
                        ]
                    }
                }
//                clients = listOf(
//                    gpt4O,
//                    gpt4OMini,
//                    perplexityLarge,
//                    perplexitySmall,
//                    claudeSonet,
//                    claudeHaiku,
//                )
                /** 벤더별 기능작동 테스트 */
                clients = listOf(
                    gpt4OMini,
                    //perplexitySmall,
                    claudeSonet,
                    claudeHaiku,
                    //deepseek,
                )
            }

            val query = arr[
                "kotlin 용 DI 프레임워크 유명한고 2개만 소개해주고, 장단점을 알려줘",
                "아이들에게 교육하는 말투로 답변해줘",
                "반드시! 결과전체를 하나의 JSON으로 만들고 json의 value에는 한글 / 영어로만 답변해줘", //퍼블렉시티는 본문에 이게 들어가야함
            ]

            Then("비교 & 프린트") {
                val results = set.executeSingle(query.toString())
                results.printSimple()
            }
        }

    }

}