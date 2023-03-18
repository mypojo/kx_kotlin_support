package net.kotlinx.okhttp.okhttp

import net.kotlinx.okhttp.HttpReq
import okhttp3.Request

//https://square.github.io/okhttp/recipes/

fun HttpReq.build(): Request {
    return Request.Builder().url(this.url).build()
}