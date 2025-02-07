package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.csv.readCsvLines
import net.kotlinx.file.slash
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.CharSets
import net.kotlinx.system.ResourceHolder

@OptIn(BetaOpenAI::class)
class AiModel_벤더별광고문구비교 : BeSpecLight() {

    /** 기본 시스템 프롬프트 */
    val SYSTEM_PROMPT = obj {
        "task" to "주어진 광고 사이트 URL 및 가이드를 참고해서 광고 제목(title)과 설명(desc)을 도출"
        "rules" to arr[
            "제목은 최대 15자",
            "설명은 최대 45자",
            "제목과 문구는 2개씩 만들어줘",
            "다른 텍스트 없이 반드시 단일 JSON array 형식으로만 응답해줘",
        ]
        "example_request" to obj {
            "site" to "https://only.webhard.co.kr/"
            "title_guide" to "LG유플러스 공식사이트를 강조"
            "desc_guide" to "저렴한 요금제와 제품의 특장점을 강조"
        }
        "example_responses" to arr[
            obj {
                "title" to "LG유플러스 공식 웹하드"
                "desc" to "저렴한 요금제와 안정적인 보안 기능으로 LG유플러스 공식 서비스를 경험하세요"
            },
            obj {
                "title" to "LG유플러스 공식"
                "desc" to "안정적인 인터넷과 스마트홈 솔루션으로 LG유플러스를 만나세요"
            },
        ]
    }.toGsonData()

    init {
        initTest(KotestUtil.IGNORE)

        Given("GPT") {

            val set = AiModelLocalSet {
                aws = aws97
                this.systemPrompt = SYSTEM_PROMPT
                /** 벤더별 기능작동 테스트 */
                clients = listOf(
                    gpt4OMini,
                    claudeHaiku,
                    perplexity01,
                    perplexity02,
                    perplexity03,
                    perplexity04,
                )
            }

            Then("단건") {
                val query = arr[
                    "https://only.webhard.co.kr/",
                    "LG유플러스 공식사이트를 강조",
                    "저렴한 요금제와 제품의 특장점을 강조",
                ]
                val results = set.executeSingle(query.toString())
                results.printSimple()
            }

            Then("대량") {

                val file = ResourceHolder.WORKSPACE.slash("AI/TND추출/검색광고_AI솔루션_T&D자동화_템플릿 예시.csv")
                val lines = file.readCsvLines(CharSets.MS949)


            }
        }

    }

}