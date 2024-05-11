package net.kotlinx.kotest

import ch.qos.logback.classic.Level
import net.kotlinx.core.CoreUtil
import net.kotlinx.logback.LogBackUtil
import net.kotlinx.system.DeploymentType


/**
 * kotest 로그
 * */
object KotestLogUtil {

    val DEPLOYMENT_TYPE by lazy { DeploymentType.load() }

    fun logLevelInit() {
        when (DEPLOYMENT_TYPE) {
            DeploymentType.DEV -> LogBackUtil.logLevelTo(CoreUtil.PACKAGE_NAME, Level.DEBUG)
            else -> LogBackUtil.logLevelTo(CoreUtil.PACKAGE_NAME, Level.INFO)
        }
    }

}