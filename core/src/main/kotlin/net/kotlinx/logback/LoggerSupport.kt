package net.kotlinx.logback

import kotlinx.coroutines.runBlocking
import mu.KLogger


fun KLogger.warns(block: suspend () -> Any?) {
    this.warn {
        runBlocking {
            block().toString()
        }
    }
}

fun KLogger.infos(block: suspend () -> Any?) {
    this.info {
        runBlocking {
            block().toString()
        }
    }
}

fun KLogger.debugs(block: suspend () -> Any?) {
    this.debug {
        runBlocking {
            block().toString()
        }
    }
}