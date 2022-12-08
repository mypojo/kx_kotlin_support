package net.kotlinx.kotlinSupport.core2.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.slf4j.impl.StaticLoggerBinder
import java.io.InputStream

object LogBackUtil {

    private val log = KotlinLogging.logger {}

    /** SLF4J 구현체 확인  */
    fun findImplStr(): String {
        return StaticLoggerBinder.getSingleton().loggerFactoryClassStr
    }

    /** 스프링 부트 설정을 베이스로 로드 (로컬 JUnit 등 스프링 부트가 아닌 경우)  */
    fun initSpringBoot() {
        try {
            initAppTest()
        } catch (e: Exception) {
            initApp()
        }
    }

    private fun initApp() {
        init("logback-app.xml")
    }

    private fun initAppTest() {
        init("logback-app-test.xml")
    }

    private fun init(configName: String) {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.reset()
        val configurator = JoranConfigurator()
        val configStream: InputStream = LogBackUtil::class.java.classLoader.getResourceAsStream(configName)!!
        configurator.context = loggerContext
        configurator.doConfigure(configStream) // loads logback file
        log.info("강제로 특정 로그백 설정({})이 로드됩니다.", configName)
    }

    /** 런타임에 레벨 조절  */
    fun logLevelRoot(toLevel: Level) {
        val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
        root.level = toLevel
    }

    /** 런타임에 레벨 조절  */
    fun logLevelTo(packageName: String, toLevel: Level) {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = loggerContext.getLogger(packageName)
        logger.level = toLevel
    }
}