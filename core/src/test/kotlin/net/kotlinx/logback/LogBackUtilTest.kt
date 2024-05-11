package net.kotlinx.logback

import ch.qos.logback.classic.Level
import net.kotlinx.core.CoreUtil
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class LogBackUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("LogBackUtil") {
            Then("레벨 변경 -> 디버그 로그 ") {
                log.trace { "trace -> 로깅되지 않음" }
                log.debug { "debug 로깅됨" }

                LogBackUtil.logLevelTo(CoreUtil.PACKAGE_NAME, Level.TRACE)

                log.trace { "trace 이제 로깅됨" }
                log.debug { "debug 로깅됨" }
            }
        }
    }

}