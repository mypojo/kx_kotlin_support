package net.kotlinx.slack.template

import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.kotlin_extension.block.withBlocks
import net.kotlinx.core.Kdsl
import net.kotlinx.core.dev.DeveloperData
import net.kotlinx.core.string.abbr
import net.kotlinx.slack.*

/**
 * 슬랙 알람 템플릿
 */
class SlackSimpleAlert : SlackMessage {

    @Kdsl
    constructor(block: SlackSimpleAlert.() -> Unit = {}) {
        apply(block)
    }

    override fun send(): String {

        mainMsg = ":warning: [$source] 에러 :warning:"
        blocks = withBlocks {

            header { text(mainMsg) }

            val workDivLinkText = if (workDivLink == null) workDiv else "${workDiv.slackLink(workDivLink!!)}"
            val slackIdsText = if (developers.isEmpty()) "" else ":hot_face: ${developers.joinToString(" / ") { it.slackId!!.slackMention() }}"
            val workLocationLinkText = if (workLocationLink == null) "" else ":floppy_disk: ${"상세로그보기".slackLink(workLocationLink!!)}"

            /** 기본 메세지 */
            val defaultMessages = listOf(
                ":id: $workDivLinkText  $slackIdsText",
                ":computer: $workLocation  $workLocationLinkText",
            )
            section {
                markdownText(defaultMessages.joinToString("\n"))
            }

            exception?.let {
                section {
                    markdownText("${it::class.simpleName!!.slackCode()} ${it.message?.abbr(50, "..") ?: "empty message"} ")
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

        return super.send()
    }

    override lateinit var channel: String
    override lateinit var mainMsg: String
    override var blocks: List<LayoutBlock> = emptyList()
    override var threadTs: String? = null

    /** 알림 소스 (프로젝트 명..) */
    lateinit var source: String

    /** 작업 구분 */
    lateinit var workDiv: String

    /** 작업 구분의 링크 (DDB 정보).. */
    var workDivLink: String? = null

    /** 작업 위치 (하드웨어 정보) */
    lateinit var workLocation: String

    /** 작업 위치 로그 (클라우드와치 링크 등..) */
    var workLocationLink: String? = null

    /** 담당자 개발자들 */
    var developers: List<DeveloperData> = emptyList()

    /** 에러 */
    var exception: Throwable? = null

    /** 설명문구 -  Block quotes */
    var descriptions: List<String> = emptyList()

    /** 본문 텍스트 - Code blocks */
    var body: List<String> = emptyList()


}

