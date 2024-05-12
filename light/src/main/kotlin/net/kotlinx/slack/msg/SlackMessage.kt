package net.kotlinx.slack.msg

import com.slack.api.model.block.LayoutBlock

/**
 * 슬랙 메세지 & 블록을 구성하는 인터페이스
 *  */
interface SlackMessage {

    /**
     * 채널 or 웹훅이 들어온다.
     * http로 시작되면 웹훅으로 간주
     * */
    var channel: String

    /**
     * 메인 메세지.
     * 블록으로 보내면, 본문에는 보이지 않지만  우하단 푸시알림에 이게 표시됨
     * 멘션시 이름 말고 <{ID}>로 해야한다 ex) <@UJ2KMTDA4>
     *  */
    var mainMsg: String

    /** 블록 구성시 입력 */
    var blocks: List<LayoutBlock>

    /** 스래드가 있으면 댓글로 추가함 */
    var threadTs: String?

}