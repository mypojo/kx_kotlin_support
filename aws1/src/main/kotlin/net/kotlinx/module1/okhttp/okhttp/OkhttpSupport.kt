package net.kotlinx.module1.okhttp.okhttp

import net.kotlinx.module1.okhttp.HttpReq
import okhttp3.Request

//https://square.github.io/okhttp/recipes/

fun HttpReq.build(): Request {
    return Request.Builder().url(this.url).build()
}