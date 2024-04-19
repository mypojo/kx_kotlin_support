package net.kotlinx.openAi

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.TextContent

/** chat 결과를 간단하게 리턴해준다. */
fun ChatCompletion.toContents(): List<String> {
    return this.choices.map { choice ->
        when (val messageContent = choice.message.messageContent!!) {
            is TextContent -> messageContent.content
            else -> messageContent.toString()
        }
    }
}