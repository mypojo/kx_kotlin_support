package net.kotlinx.aws.bedrock

import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import io.ktor.util.*
import net.kotlinx.ai.AiTextInput
import net.kotlinx.ai.AiTextInput.AiTextImage
import net.kotlinx.json.koson.toKsonArray


/**
 * 인보커용 컨버터
 * */
object BedrockTextInvokerConverter {

    fun convert(client: BedrockTextClient, inputs: List<AiTextInput>): ObjectType {
        val data = obj {
            "anthropic_version" to "bedrock-2023-05-31" //??
            client.inferenceConfig?.maxTokens?.let { "max_tokens" to it }
            client.inferenceConfig?.temperature?.let { "temperature" to it }
            client.inferenceConfig?.topP?.let { "topP" to it } // topK 는 없네?? 일단 뺀다
            "system" to client.systemPrompt.toString()
            "messages" to arr[
                obj {
                    "role" to "user"
                    "content" to inputs.map { input ->
                        when (input) {
                            is AiTextImage -> {
                                val file = input.file ?: throw IllegalStateException("Bedrock Input File Not Found")
                                obj {
                                    "type" to "image"
                                    "source" to obj {
                                        "type" to "base64" //지금은 base64 만 지원하는듯. s3 안됨
                                        "media_type" to "image/${file.extension.lowercase()}"
                                        "data" to file.readBytes().encodeBase64()
                                    }
                                }
                            }

                            is AiTextInput.AiTextString -> obj {
                                "type" to "text"  //'text', 'image', 'tool_use', 'tool_result'
                                "text" to input.text
                            }

                        }

                    }.toKsonArray()
                },
            ]
        }
        return data
    }
}