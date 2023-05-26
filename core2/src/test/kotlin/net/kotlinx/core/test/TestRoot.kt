package net.kotlinx.core.test

import ch.qos.logback.classic.Level
import mu.KotlinLogging
import net.kotlinx.core.CoreUtil
import net.kotlinx.core.logback.LogBackUtil
import net.kotlinx.core.time.TimeStart
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.IOException

/**
 * Junit5기반 테스트를 상속해서 사용하는 도우미
 * 샘플 JVM 옵션 : -Xms128m -Xmx8048m -Dfile.encoding=UTF-8 --illegal-access=warn -Dsetup=TEST
 */
abstract class TestRoot {

    /** 필요시 구현하기  */
    fun beforeDefault() {
        log.trace { "아무것도 하지않음" }
    }

    @BeforeEach
    fun before(testInfo: TestInfo) {
        beforeDefault()
    }

    companion object {

        val log = KotlinLogging.logger {}
        private lateinit var start: TimeStart

        //==================================================== 기본 구현 ======================================================
        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            LogBackUtil.logLevelTo(CoreUtil.packageName, Level.DEBUG)
            start = TimeStart()
        }

        /** 매번 테스트 종료시마다 호출된다.  */
        @AfterAll
        @JvmStatic
        @Throws(IOException::class)
        fun afterClass() {
            log.info { "테스트 종료 $start" }
        }
    }
}