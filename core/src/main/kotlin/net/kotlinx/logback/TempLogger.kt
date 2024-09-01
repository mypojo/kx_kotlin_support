package net.kotlinx.logback

import ch.qos.logback.classic.Level
import net.kotlinx.time.TimeFormat

/**
 * 그래들 등에서 로그 조잘하기 힘들어서 만든 임시 로깅 도구
 * */
class TempLogger(val logLevel: Level) {

    private fun print(level: Level, text: String) {
        if (logLevel.levelInt > level.levelInt) return
        println("${TimeFormat.YMDHMS_F01.get()} $level $text")
    }

    fun trace(msg: () -> Any?) = print(Level.TRACE, msg().toString())

    fun debug(msg: () -> Any?) = print(Level.DEBUG, msg().toString())

    fun info(msg: () -> Any?) = print(Level.INFO, msg().toString())

    fun warn(msg: () -> Any?) = print(Level.WARN, msg().toString())

    fun error(msg: () -> Any?) = print(Level.ERROR, msg().toString())

}