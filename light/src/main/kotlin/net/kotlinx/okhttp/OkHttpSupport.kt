package net.kotlinx.okhttp

import okhttp3.OkHttpClient


private val OKHTTP_REQ_INTERCEPTOR: MutableMap<OkHttpClient, (OkHttpReq) -> Unit> = mutableMapOf()

/** interceptor 설정 추가 */
var OkHttpClient.reqInterceptor: (OkHttpReq) -> Unit
    get() = OKHTTP_REQ_INTERCEPTOR.getOrDefault(this) {}
    set(value) {
        OKHTTP_REQ_INTERCEPTOR[this] = value
    }



