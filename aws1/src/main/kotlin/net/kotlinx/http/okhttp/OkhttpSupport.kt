package net.kotlinx.http.okhttp

import net.kotlinx.http.HttpReq
import okhttp3.Request

//https://square.github.io/okhttp/recipes/

fun HttpReq.build(): Request {
    return Request.Builder().url(this.url).build()
}