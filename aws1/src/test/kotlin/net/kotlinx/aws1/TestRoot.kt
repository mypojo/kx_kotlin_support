package net.kotlinx.aws1

import ch.qos.logback.classic.Level
import mu.KotlinLogging
import net.kotlinx.core1.CoreUtil
import net.kotlinx.core2.logback.LogBackUtil
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.IOException

/**
 * Junit5기반 테스트를 상속해서 사용하는 도우미
 */
abstract class TestRoot {

    private val log = KotlinLogging.logger {}

    /** 필요시 구현하기  */
    fun beforeDefault() {
        log.debug { "아무것도 하지않음" }
    }

    @BeforeEach
    fun before(testInfo: TestInfo) {
        beforeDefault()
    }

    companion object {

        private val log = KotlinLogging.logger {}

        //==================================================== 기본 구현 ======================================================
        @JvmStatic
        @BeforeAll
        fun beforeClass() {
            LogBackUtil.logLevelTo(CoreUtil.packageName, Level.DEBUG)
        }

        /** 매번 테스트 종료시마다 호출된다.  */
        @JvmStatic
        @AfterAll
        @Throws(IOException::class)
        fun afterClass() {
            log.info { "테스트 종료" }
        }
    }
}