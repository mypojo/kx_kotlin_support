package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.thread.Thread
import com.aallam.openai.client.OpenAI

/**

 *  */
@OptIn(BetaOpenAI::class) //스래드 사용
class OpenAiThread(
    val openAi: OpenAI,
    var thread: Thread,
) {

    /** 단순 채팅 질문 */
    fun chat(msgs: List<String>){
//
//        openAi.messages()
//
//        val start = TimeStart()
//        val reqs = ChatCompletionRequest(
//            model = ModelId(modelId),
//            messages = fixedMessages + msgs.map { ChatMessage(role = ChatRole.User, content = it) },
//
//            )
//        val completion = openAi.chatCompletion(reqs)
//        log.debug { " -> [$modelId] 걸린시간 $start" }
//        return completion
    }


}