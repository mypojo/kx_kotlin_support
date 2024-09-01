package net.kotlinx.gradle

import ch.qos.logback.classic.Level
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.logback.TempLogger

class GradleLoggerTest : BeSpecLog() {


    init {
        initTest(KotestUtil.FAST)

        Given("로깅테스트") {

            val logger = TempLogger(Level.DEBUG)

            Then("간단 출력") {
                logger.trace { "트레이스" }
                logger.debug { "디버그" }
                logger.info { "인포" }
                logger.warn { "warn" }
            }
        }
    }


}
