package net.kotlinx.kotlinSupport.core2

import mu.KotlinLogging
import net.kotlinx.kotlinSupport.string.toLocalDateTime
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

private val log = KotlinLogging.logger {}

fun main() {


    //코틀린 로깅 샘플
    log.info { "우리동네 영감님" }
    log.warn { "우리동네 영감님2" }

    val text = "temp1/aa2011%3D222.png"
    println(text)
    println(URLEncoder.encode(text, Charsets.UTF_8.name()))
    println(URLDecoder.decode(text, Charsets.UTF_8.name()))
    println(Base64.getDecoder().decode(text))

    "asd".toLocalDateTime()



}