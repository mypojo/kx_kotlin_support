package net.kotlinx.time

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

/** 밀리초로 간주하고 문자로 변환 (로그 확인용) */
fun Long.toTimeString(): TimeString = TimeString(this)

/** suspend 지원하는 시간 측정기 */
fun measureTimeString(block: suspend () -> Unit): TimeString = measureTimeMillis {
    runBlocking { block() }
}.toTimeString()

/** suspend 지원하는 시간 측정기 */
fun measureTimePrint(block: suspend () -> Unit) {
    val start = TimeStart()
    runBlocking { block() }
    val log = KotlinLogging.logger {}
    log.warn { "걸린시간 : ${start}" }
}

/** suspend 지원하는 시간 측정기 - 간단버전 */
fun measureTime(block: suspend () -> Any?): Any? {
    val log = KotlinLogging.logger {}
    var result: Any? = null
    val millis = measureTimeMillis {
        result = runBlocking { block() }
    }
    log.debug { " -> ${millis.toTimeString()}" }
    return result
}