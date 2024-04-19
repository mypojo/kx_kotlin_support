package net.kotlinx.okhttp

import okhttp3.HttpUrl

/** 간단 addQueryParameter */
fun HttpUrl.Builder.addQueryParameter(map: Map<String, String>): HttpUrl.Builder {
    map.entries.forEach { addQueryParameter(it.key, it.value) }
    return this
}