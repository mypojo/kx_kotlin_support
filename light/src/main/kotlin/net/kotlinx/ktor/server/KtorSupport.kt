package net.kotlinx.ktor.server

import io.ktor.server.plugins.*
import io.ktor.server.request.*


/** IP 리턴 */
val ApplicationRequest.forwardedIp: String
    get() {
        val remoteIp: String = this.headers["x-forwarded-for"]?.split(",")?.first() ?: this.origin.remoteHost
        check(remoteIp.isNotEmpty()) { "Remote IP is empty" }
        return remoteIp
    }