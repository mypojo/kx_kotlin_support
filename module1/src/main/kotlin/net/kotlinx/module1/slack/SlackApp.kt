package net.kotlinx.module1.slack

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import mu.KotlinLogging
import net.kotlinx.core.gson.GsonData
import java.io.Closeable

/**
 * 확장함수로 해결이 불가능함
 *
 * https://slack.dev/java-slack-sdk/guides/web-api-basics 참고
 * 볼트 프레임워크가 아닌 기본 API 사용.
 *
 * OAuth & Permissions 에서 퍼미션 정한 후 토큰 발급받으면 됨. 해당 채털에 봇이 추가되어 있어야함
 * 요청 제한 있음. ->  해결하는 방법도 있음
 * => AsyncMethodsClient 는 메트릭 데이터를 매우 잘 고려합니다. 앱의 클라이언트가 이미 짧은 기간 내에 너무 많은 요청을 보낸 경우 속도 제한 오류를 피하기 위해 API 요청을 지연시킬 수 있습니다.
 */
class SlackApp(
    private val token: String
) : Closeable {

    /** 별 문제없어서 그냥 이렇게 쓴다 */
    val slack: Slack by lazy { Slack.getInstance() }

    private val log = KotlinLogging.logger {}

    /**
     * @param channel 앞에 #이 들어간 채널명 ex)  #random, 채널이 아닌 슬랙ID가 입력되면 app의 bot에 메세지가 출력된다 (나만 보임)
     * @param text     멘션시 이름 말고 <{ID}>로 해야한다 ex) <@UJ2KMTDA4>
     * @return 댓글 작성시 사용되는 글의 ID
     */
    fun chatPostMessage(channel: String, text: String): String {
        val request: ChatPostMessageRequest = ChatPostMessageRequest.builder().channel(channel).text(text).build()
        val response: ChatPostMessageResponse = slack.methods(token).chatPostMessage(request)
        if (!response.isOk) {
            log.warn { "채널[${channel}] 에러 : ${response.error}" }
        }
        return response.ts
    }

    /**
     * TS를 알고있는경우 해당 메세지의 스래드에 글 추가
     */
    fun chatPostMessageReply(channel: String, ts: String, text: String): String {
        val request: ChatPostMessageRequest = ChatPostMessageRequest.builder().channel(channel).text(text).threadTs(ts).build()
        val response: ChatPostMessageResponse = slack.methods(token).chatPostMessage(request)
        if (!response.isOk) {
            log.warn { "채널[${channel}] 에러 : ${response.error}" }
        }
        return response.ts
    }

    override fun close() = slack.close()

    companion object {

        //==================================================== static ======================================================
        /**
         * 간단 인라인 메세지 발송기.
         * ex) 일회용 컨테이너 등에서 메세지 발송
         */
        fun chat(token: String, channel: String, text: String): String = SlackApp(token).let {
            it.chatPostMessage(channel, text).apply {
                it.close()
            }
        }

        /** 웹훅용  json 생성. 웹훅의 경우 잘못 보내면 전송 안되지만 그래도 OK를 리턴한다. 확인필수  */
        fun toWebHooks(msg: String): GsonData = GsonData.obj().apply { put("text", msg) }
    }
}