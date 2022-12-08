package net.kotlinx.kotlinSupport.core2.logback

import mu.KotlinLogging
import org.junit.jupiter.api.Test

internal class LogBackUtilTest {

    private val log = KotlinLogging.logger {}

    @Test
    fun `기본테스트`() {


        log.trace { "trace 영감님" }
        log.debug { "debug 영감님" }

        LogBackUtil.logLevelTo("net.kotlinx", ch.qos.logback.classic.Level.DEBUG)

        log.trace { "trace 영감님" }
        log.debug { "debug 영감님" }
    }

}