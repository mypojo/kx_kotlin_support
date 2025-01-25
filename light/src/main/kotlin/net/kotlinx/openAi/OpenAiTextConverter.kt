package net.kotlinx.openAi

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.ListContent
import com.aallam.openai.api.chat.TextPart
import net.kotlinx.ai.AiTextInput
import net.kotlinx.ai.AiTextInput.AiTextImage


object OpenAiTextConverter {

    fun convert(inputs: List<AiTextInput>): ChatMessage {
        return ChatMessage(
            role = ChatRole.User, messageContent = ListContent(
                inputs.map { input ->
                    when (input) {
                        is AiTextImage -> {
                            val url = input.url ?: throw IllegalStateException("OpenAi Input url is required")
                            ImagePart(url)
                        }

                        is AiTextInput.AiTextString -> {
                            TextPart(input.text)
                        }
                    }
                }
            )
        )
    }


}