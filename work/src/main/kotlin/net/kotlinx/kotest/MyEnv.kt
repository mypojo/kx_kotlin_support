package net.kotlinx.kotest

import ch.qos.logback.classic.Level
import mu.KotlinLogging
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.logback.LogBackUtil
import net.kotlinx.system.DeploymentType


/** 기본 로그레벨 설정 */
val DeploymentType.LOG_LEVEL: Level
    get() = when (this) {
        DeploymentType.PROD -> Level.INFO
        DeploymentType.DEV -> Level.DEBUG
    }

/**
 * 각종 환경설정 캐싱
 */
object MyEnv {

    private val log = KotlinLogging.logger {}

    //==================================================== 프로젝트 메타데이터 ======================================================
    /** 프로젝트 명  */
    const val PN = "kx"

    //==================================================== 프로젝트 배포환경 ======================================================

    /** 로컬 환경인지?  */
    val IS_LOCAL: Boolean
        get() = AwsInstanceTypeUtil.INSTANCE_TYPE === AwsInstanceType.LOCAL

    /**
     * WAS인경우 Spring profiles 로 고정된다.
     * BATCH나 Lambda인 경우 설정으로 파악한다.
     */
    val DEPLOYMENT_TYPE: DeploymentType = DeploymentType.load().also {
        log.warn { "DeploymentType load : $it" }
        if (IS_LOCAL && it === DeploymentType.PROD) {
            log.warn("##################### LOCAL 에서 PROD 환경 감지 #####################")
            log.warn("##################### LOCAL 에서 PROD 환경 감지 #####################")
            log.warn("##################### LOCAL 에서 PROD 환경 감지 #####################")
        }
    }

    /** 실서버인지?  */
    val IS_PROD: Boolean
        get() = DEPLOYMENT_TYPE === DeploymentType.PROD


    /**
     * 리소스 이름이 환경에 따라 달라짐
     * 실서버가 아닌경우 전부 dev로 간주
     * 네이밍 정책상 소문자로 간주한다.
     */
    val SUFFIX: String by lazy { DEPLOYMENT_TYPE.name.lowercase() }

    /** 테스트환경 등에서 로그백 로그를 디버깅으로 */
    fun logbackLevelTo(level: Level = Level.DEBUG) {
        LogBackUtil.logLevelTo("net.kotlinx", level)
        log.info { "로깅 환경을 강제 전환합니다. -> $level" }
    }
}



