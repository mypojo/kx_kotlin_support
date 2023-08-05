package net.kotlinx.module.eventLog


open class EventWeb : AbstractEvent() {

    /** http 호출한 사용자의 IP  */
    lateinit var clientIp: String

    /** 역할 전환 전 사용자 (로그인한 사용자)  */
    var userLoginId: String? = null

    /** 역할 전환 후 사용자  */
    var userId: String? = null

}