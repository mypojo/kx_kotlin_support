package net.kotlinx.okhttp

import net.kotlinx.core.concurrent.coroutineExecute
import net.kotlinx.core.test.TestRoot
import net.kotlinx.core.time.TimeStart
import net.kotlinx.core.time.measureTimeString
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test

class OkHttpSamplesTest : TestRoot() {

    @Test
    fun test() {

        val client = OkHttpClient()
        OkHttpSamples.dollarWonFetch(client)

        log.warn { "=========================================" }
        measureTimeString {
            (0..3).map {
                suspend {
                    val start = TimeStart()
                    log.info { "작업시작 $it .." }
                    val won = OkHttpSamples.dollarWonFetch(client)
                    log.info { "작업종료 $it -> $won  $start" }
                }
            }.coroutineExecute()
        }.also {
            log.warn { "=== 동기 $it" }
        }

        log.warn { "=========================================" }
        measureTimeString {
            (0..3).map {
                suspend {
                    val start = TimeStart()
                    log.info { "작업시작 $it .." }
                    val won = OkHttpSamples.dollarWon(client)
                    log.info { "작업종료 $it -> $won  $start" }
                }
            }.coroutineExecute()
        }.also {
            log.warn { "=== 비동기 $it" }
        }


    }

}