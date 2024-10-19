package net.kotlinx.slack.msg

import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.kotlin_extension.block.withBlocks
import net.kotlinx.core.Kdsl
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.number.ifFalse
import net.kotlinx.slack.*
import net.kotlinx.string.abbr

/**
 * 슬랙 알람 템플릿
 */
class SlackSimpleAlert : SlackMessage {

    @Kdsl
    constructor(block: SlackSimpleAlert.() -> Unit = {}) {
        apply(block)
        this::mainMsg.isInitialized.ifFalse {
            mainMsg = when {
                exception != null -> ":warning: [$source] 에러 :warning:"
                else -> ":white_check_mark: [$source] 메세지 :white_check_mark:"
            }
        }
        blocks = withBlocks {

            header { text(mainMsg) }

            val workDivLinkText = if (workDivLink == null) workDiv else workDiv.slackLink(workDivLink!!)
            val workLocationLinkText = if (workLocationLink == null) workLocation else workLocation.slackLink(workLocationLink!!)
            val slackIdsText = if (developers.isEmpty()) "" else ":smile: ${developers.joinToString(" / ") { it.slackId!!.slackMention() }}"

            /** 기본 메세지 */
            /** 기본 메세지 */
            val defaultMessages = listOf(
                ":id: $workDivLinkText :computer: $workLocationLinkText  $slackIdsText",
            )
            section {
                markdownText(defaultMessages.joinToString("\n"))
            }

            exception?.let {
                section {
                    markdownText("${it::class.simpleName!!.slackCode()} ${it.message?.abbr(200, "..") ?: "empty message"} ")
                }
            }

            if (descriptions.isNotEmpty()) {
                section {
                    markdownText(descriptions.joinToString("\n") { it.slackBlockQuote() })
                }
            }
            if (body.isNotEmpty()) {
                section {
                    markdownText(body.joinToString("\n").slackQuote())
                }
            }
        }
    }

    override lateinit var channel: String
    override lateinit var mainMsg: String
    override var blocks: List<LayoutBlock> = emptyList()
    override var threadTs: String? = null

    /**
     * 알림 소스
     * ex) 프로젝트 명
     * */
    lateinit var source: String

    /**
     * 작업 구분
     * ex) job 이름 등.
     * */
    lateinit var workDiv: String

    /**
     * 작업 구분의 링크
     * ex) 작업 스펙 명세서, 작업의 실행 DDB 정보 링크..
     * */
    var workDivLink: String? = null

    /**
     * 작업 위치
     * ex) 하드웨어 정보, lambda, AWS BATCh, ECS 등등..
     * */
    lateinit var workLocation: String

    /**
     * 작업 위치 로그
     * ex) 클라우드와치 링크
     * */
    var workLocationLink: String? = null

    /** 담당자 개발자들 */
    var developers: List<DeveloperData> = emptyList()

    /** 에러 */
    var exception: Throwable? = null

    /**
     * 이 값이 있으면 특정 채널이 아니라 개인 채널로 보낸다. ID를 입력하면됨
     * ex) U0641U84xxx
     *  */
    var toUser: String? = null

    /**
     * 설명문구 -  Block quotes
     * 링크 삽입 가능!!
     *  */
    var descriptions: List<String> = emptyList()

    /**
     * 본문 텍스트 - Code blocks
     * 링크 삽입 불가능함!! 주로 코드나 표, SQL 문구 등 표기용
     * */
    var body: List<String> = emptyList()

}

