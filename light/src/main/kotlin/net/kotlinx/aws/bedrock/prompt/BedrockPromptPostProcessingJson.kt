package net.kotlinx.aws.bedrock.prompt

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.core.Kdsl

/**
 * 배드락 포스트-프로세싱 프롬프트 생성기
 * - 함수 호출형 에이전트의 최신 응답을 사용자 친화적으로 변환하기 위한 system / messages 구조를 생성한다.
 * - 기본 템플릿은 제공된 원문을 그대로 포함하며, `$question$`, `$latest_response$`, `$responses$` 치환 변수를 유지한다.
 * - 필요 시 헤더/푸터 및 사용자 지시문을 커스터마이즈 할 수 있다.
 */
class BedrockPromptPostProcessingJson {

    @Kdsl
    constructor(block: BedrockPromptPostProcessingJson.() -> Unit = {}) {
        apply(block)
    }

    /** system 프롬프트 상단에 추가할 사용자 정의 헤더 (선택) -> 직접 만들지 말고 LLM 한테 변환해달라고 할것 */
    var header: String = ""

    /** system 프롬프트 하단에 추가할 사용자 정의 푸터 (선택) */
    var footer: String = ""

    /**
     * 기본 system 템플릿 (원문 반영)
     * - `$question$`, `$latest_response$`, `$responses$` 는 런타임에 외부에서 문자열 치환되는 것을 전제로 그대로 둔다.
     */
    var systemTemplate: String = $$"""
Guidelines:
- Always output **only valid JSON** inside the <final_response></final_response> XML tags.
- Do not add any natural language explanations or comments outside the JSON.
- Do not expose any function or API names used internally.
- Ensure the JSON object is syntactically valid and self-contained.

Here’s the original user question:
<user_input>$question$</user_input>

Here’s the raw output from the function-calling agent:
<latest_response>$latest_response$</latest_response>

Here’s the history of all actions taken in this session:
<history>$responses$</history>

""".trimIndent()

    /**
     * 사용자에게 전달할 메시지(기본값은 원문 예시의 안내)
     * - 필요 시 변경 가능
     */
    var userInstruction: String = "Please output only your final structured JSON within <final_response></final_response> tags"

    /**
     * JSON 문자열로 직렬화된 프롬프트를 반환한다.
     * - PreProcessing/Orchestration 과 동일하게 koson을 사용해 구조를 맞춘다.
     */
    override fun toString(): String {
        val systemPrompt = buildString {
            if (header.isNotBlank()) appendLine(header)
            appendLine(systemTemplate)
            if (footer.isNotBlank()) appendLine().appendLine(footer)
        }.trimEnd()

        return obj {
            // Anthropic Bedrock 형식을 일부 구현 (PreProcessing 과 동일하게 anthropic_version 은 생략)
            "system" to systemPrompt
            "messages" to arr[
                obj {
                    "role" to "user"
                    "content" to arr[
                        obj { "text" to userInstruction }
                    ]
                }
            ]
        }.toString()
    }
}
