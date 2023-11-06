package net.kotlinx.slack

/**
 * 슬렉 메세지를 구성하는 템플릿에 붙임
 * */
interface SlackMsg {
    fun chatPostMessage()
}