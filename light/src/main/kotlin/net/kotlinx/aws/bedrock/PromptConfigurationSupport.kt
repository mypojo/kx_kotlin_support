package net.kotlinx.ai.prompt

import aws.sdk.kotlin.services.bedrockagent.model.PromptConfiguration
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint

/**
 * Bedrock Agent의 [PromptConfiguration] 리스트를 그리드로 출력하는 보조 확장 함수들
 */

/**
 * 상세 출력: 템플릿 본문까지 포함해서 출력
 */
fun List<PromptConfiguration>.printSimple() {
    val headers = listOf("promptType", "promptCreationMode", "promptState", "basePromptTemplate")
    headers.toTextGridPrint {
        this@printSimple.map {
            arrayOf(
                it.promptType,
                it.promptState,
                it.promptCreationMode,
                it.basePromptTemplate?.trim()?.replace("\n", "")?.abbr(60)
            )
        }
    }
}
