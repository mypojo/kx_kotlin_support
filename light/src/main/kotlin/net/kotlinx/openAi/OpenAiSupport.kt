package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.file.File
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.message.MessageContent
import mu.KotlinLogging
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.number.toSiText
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01


/** 첫번째 매칭 & 마지막 매칭으로 잘라준다 */
fun String.substringBetween(range: Pair<String, String>): String = this.substringAfter(range.first).substringBeforeLast(range.second).trim()

/** chat 결과를 간단하게 리턴해준다. */
fun ChatCompletion.toListString(): List<String> {
    val log = KotlinLogging.logger {}
    return this.choices.map { choice ->
        when (val messageContent = choice.message.messageContent!!) {
            is TextContent -> {
                val text = messageContent.content
                when {
                    text.contains("```json") -> text.substringBetween("```json" to "```")
                    else -> text
                }
            }

            else -> {
                log.warn { "!!! 문자열 형식 확인필요 !!! -> ${messageContent::class}" }
                messageContent.toString()
            }
        }
    }
}


/**
 * 결과 간단 문자열화
 * 정해진 고정 json or 화면 출력용
 *  */
@OptIn(BetaOpenAI::class)
fun List<Message>.toListString(): List<String> {
    return this.flatMap { msg ->
        msg.content.map { c ->
            when (c) {
                is MessageContent.Text -> {
                    val text = c.text.value
                    when {
                        text.startsWith("```json") -> text.substringBetween("```json" to "```")
                        else -> text
                    }

                }

                else -> {
                    c.toString()
                }
            }
        }
    }
}

//==================================================== 간단 출력 ======================================================

fun List<File>.printSimple() {
    listOf("id", "purpose", "filename", "createdAt", "용량").toTextGridPrint {
        this.sortedByDescending { it.createdAt }.map {
            arrayOf(it.id.id, it.purpose.raw, it.filename.abbr(30), it.createdAt.toLocalDateTime().toKr01(), it.bytes.toLong().toSiText())
        }
    }
}