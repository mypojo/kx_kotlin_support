package net.kotlinx.slack

import net.kotlinx.slack.msg.SlackSimpleAlert

/**
 * 슬랙 전송이 가능한 모듈
 * 1. 전송채널을 선택하는 로직
 * 2. 메세지 커스터마이징 로직
 *
 * ex) 에러 알림은 배포 환경별 개발자 채널로
 * ex) 비지니스 이슈는 각 부서 담당자 채널로
 *
 * ex) val ALERT = object : SlackSender { .. }
 *  */
interface SlackSender {
    fun send(block: SlackSimpleAlert.() -> Unit)
}