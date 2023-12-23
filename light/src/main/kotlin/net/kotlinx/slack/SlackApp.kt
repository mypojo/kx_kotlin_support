package net.kotlinx.slack

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.webhook.WebhookPayloads
import mu.KotlinLogging
import net.kotlinx.core.gson.GsonData
import java.io.Closeable

/**
 * 확장함수로 해결이 불가능해서 래퍼로 만듬 (토큰!!)
 * https://api.slack.com/apps 여기서 앱 확인
 * 테스트 페이지 링크 : https://api.slack.com/methods/chat.postMessage/test
 *
 * https://slack.dev/java-slack-sdk/guides/web-api-basics
 * 볼트 프레임워크가 아닌 기본 API 사용.
 *
 * https://api.slack.com/apps
 * #1. 슬랙 페이지 들어가서 앱 생성 (기존 설정 Manifest로 복사 가능)
 * #2. Settings -> Installed 하면 토큰 나옴 -> SSM에 저장
 * #3. 슬랙에서 채널 만들고 만들었던 앱 추가.
 *
 * 요청 제한 있음. ->  해결하는 방법도 있음
 * => AsyncMethodsClient 는 메트릭 데이터를 매우 잘 고려합니다. 앱의 클라이언트가 이미 짧은 기간 내에 너무 많은 요청을 보낸 경우 속도 제한 오류를 피하기 위해 API 요청을 지연시킬 수 있습니다.
 *
 * 블록 참고사항
 * https://app.slack.com/block-kit-builder
 *
 * 토큰확인 (봇/유저)
 * https://api.slack.com/apps -> Features -> OAuth & Permissions
 */
class SlackApp(
    /**
     * 4가지 토큰 유형이 있음. bot or user 토큰
     * https://api.slack.com/authentication/token-types#user
     *  */
    private val token: String
) : Closeable {

    /** 별 문제없어서 그냥 이렇게 쓴다.  private 아님! */
    val slack: Slack by lazy { Slack.getInstance() }

    override fun close() = slack.close()

    private val log = KotlinLogging.logger {}

    /**
     * 기본 DSL 버전
     * TS를 알고있는경우 해당 메세지의 스래드에 글 추가 가능
     * @return 댓글 작성시 사용되는 글의 ID
     *  */
    fun chatPostMessage(block: ChatPostMessageRequest.ChatPostMessageRequestBuilder.() -> Unit): String {
        val request: ChatPostMessageRequest = ChatPostMessageRequest.builder().apply(block).build()
        val response: ChatPostMessageResponse = slack.methods(token).chatPostMessage(request)
        if (!response.isOk) {
            log.warn { "채널[${request.channel}] 에러 : ${response.error}" }
        }
        return response.ts ?: throw IllegalStateException("채널[${request.channel}] 에러 : ${response.error}")
    }

    /**
     * 메세지 전송
     * @return 댓글 작성시 사용되는 글의 ID  (웹훅은 일단 안되는듯함)
     * TS 관련 https://copyprogramming.com/howto/slack-get-thread-id-after-posting-message-using-incoming-web-hook
     * 인커밍훅 관련 https://slack.dev/java-slack-sdk/guides/incoming-webhooks
     *  */
    fun send(msg: SlackMessage): String {
        if (msg.channel.startsWith("http")) {
            log.trace { "웹훅으로 전송합니다.." }
            val payload = WebhookPayloads.payload {
                it.text(msg.mainMsg)
                msg.threadTs?.let { v -> it.threadTs(v) }
                it.blocks(msg.blocks)
            }
            val response = slack.send(msg.channel, payload)!!
            if (response.code != 200) {
                log.warn { "채널[${msg.channel}] 에러 : ${response.message}" }
            }
            return "ok"
        } else {
            return chatPostMessage{
                text(msg.mainMsg)
                channel(msg.channel)
                msg.threadTs?.let { v -> threadTs(v) }
                blocks(msg.blocks)
            }
        }
    }


    companion object {

        //==================================================== static ======================================================


        /**
         * 클라이언트 없이 간이 웹훅 전송시 사용함.
         * 웹훅용  json 생성 -> 웹훅의 경우 잘못 보내면 전송 안되지만 그래도 OK를 리턴한다. 확인필수
         *  */
        fun toWebHooks(msg: String): GsonData = GsonData.obj().apply { put("text", msg) }
    }
}