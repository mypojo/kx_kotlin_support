package net.kotlinx.aws.bedrock.prompt

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.core.Kdsl

/**
 * 배드락 간단 프롬프트 생성기
 * 전처리 할때 사용한다
 * */
class BedrockPromptPreProcessing {

    @Kdsl
    constructor(block: BedrockPromptPreProcessing.() -> Unit = {}) {
        apply(block)
    }

    lateinit var header: String
    lateinit var footer: String

    var function = $$"""
Here is the list of functions we are providing to our function calling agent. The agent is not allowed to call any other functions beside the ones listed here:
<functions>
$functions$
</functions>
    """.trimIndent()

    var conversationHistory = $$"""
The conversation history is important to pay attention to because the user's input may be building off of previous context from the conversation.
<conversation_history>
$conversation_history$
</conversation_history>
    """.trimIndent()

    override fun toString(): String {
        val prompt = """
            ${header}
            
            ${function}
            
            ${conversationHistory}
            
            ${footer}
        """.trimIndent()
        return obj {
            "system" to prompt
            "messages" to arr[
                obj {
                    "role" to "user"
                    "content" to arr[
                        obj { "text" to $$"입력: $question$" }
                    ]
                }
            ]
        }.toString()
    }


}