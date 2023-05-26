package net.kotlinx.core.logback

import ch.qos.logback.classic.Level
import mu.KotlinLogging
import net.kotlinx.core.CoreUtil
import org.junit.jupiter.api.Test

internal class LogBackUtilTest {

    private val log = KotlinLogging.logger {}

    @Test
    fun 기본테스트() {


        log.trace { "trace 영감님" }
        log.debug { "debug 영감님" }

        LogBackUtil.logLevelTo(CoreUtil.packageName, Level.DEBUG)

        log.trace { "trace 영감님" }
        log.debug { "debug 영감님" }
    }

}