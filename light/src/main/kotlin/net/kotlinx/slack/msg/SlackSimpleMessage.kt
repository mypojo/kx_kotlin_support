package net.kotlinx.slack.msg

import com.slack.api.model.block.LayoutBlock
import net.kotlinx.core.Kdsl

/**
 * 간단 인라인 메세지 발송기.
 * ex) 일회용 컨테이너 등에서 메세지 발송
 */
class SlackSimpleMessage : SlackMessage {

    @Kdsl
    constructor(block: SlackSimpleMessage.() -> Unit = {}) {
        apply(block)
    }

    override lateinit var channel: String
    override lateinit var mainMsg: String
    override var blocks: List<LayoutBlock> = emptyList()
    override var threadTs: String? = null


}

