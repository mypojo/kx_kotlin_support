package net.kotlinx.core.test

import ch.qos.logback.classic.Level
import io.kotest.core.Tag
import io.kotest.core.spec.Spec
import mu.KotlinLogging
import net.kotlinx.core.CoreUtil
import net.kotlinx.core.DeploymentType
import net.kotlinx.core.lib.SystemSeparator
import net.kotlinx.core.logback.LogBackUtil

/**
 * 런타임에 태그를 확인하는게 아직 불가능한거 같다.
 * 이때문에 일단 이렇게 임시로 사용..
 * */
fun Spec.init(vararg tags: Tag) {
    tags(*tags)
    if (!SystemSeparator.IS_GRADLE) {
        //그래들 환경이 아니라면 테스트 중으로 간주한다.
        val log = KotlinLogging.logger {}
        log.debug { "kotest TESTING TAG 추가.." }
        tags(KotestUtil.TESTING)
    }
    when (KotestUtil.DEPLOYMENT_TYPE) {
        DeploymentType.DEV -> LogBackUtil.logLevelTo(CoreUtil.PACKAGE_NAME, Level.DEBUG)
        else -> LogBackUtil.logLevelTo(CoreUtil.PACKAGE_NAME, Level.INFO)
    }
}

/**
 *
 * ex) tags(KotestUtil.FAST, KotestUtil.TESTING)
 * */
object KotestUtil {

    val DEPLOYMENT_TYPE by lazy { DeploymentType.load() }

    /** 일반적인 빠른 테스트 */
    val FAST = Tag("fast")

    /** 스래드 등의 느린 테스트 */
    val SLOW = Tag("slow")

    /** 시크릿 값이 필요한 테스트.  DDB 접근 등 */
    val PROJECT = Tag("project")

    /** 테스트중인거 */
    val TESTING = Tag("testing")

}