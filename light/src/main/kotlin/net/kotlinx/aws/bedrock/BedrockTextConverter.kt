package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockruntime.model.*
import net.kotlinx.ai.AiTextInput
import net.kotlinx.ai.AiTextInput.AiTextImage


object BedrockTextConverter {

    fun convert(inputs: List<AiTextInput>): Message {
        return Message {
            role = ConversationRole.User
            content = inputs.map { input ->
                when (input) {
                    is AiTextImage -> {
                        val fileName = input.name
                        ContentBlock.Image(
                            ImageBlock {
                                this.format = when {
                                    fileName.endsWith(".png", true) -> ImageFormat.Png
                                    fileName.endsWith(".jpg", true) -> ImageFormat.Jpeg
                                    fileName.endsWith(".jpeg", true) -> ImageFormat.Jpeg
                                    fileName.endsWith(".gif", true) -> ImageFormat.Gif
                                    else -> throw IllegalArgumentException("지원되지 않는 이미지 파일입니다. $fileName")
                                }
                                this.source = ImageSource.Bytes(input.byteArray)
                            }
                        )
                    }

                    is AiTextInput.AiTextString -> {
                        ContentBlock.Text(input.text)
                    }
                }
            }
        }

    }
}