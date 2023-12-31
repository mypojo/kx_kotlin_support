package net.kotlinx.core

import mu.KotlinLogging

/**
 * AWS 실제 인프라가 구성되는 배포 타입.
 * 샘플로 사용
 */
enum class DeploymentType {
    /**
     * 운영서버
     */
    PROD,

    /** 개발서버  */
    DEV
    ;

    companion object {

        private val log = KotlinLogging.logger {}

        /**
         * 베이스가 되는 배포 환경 로드
         * 일반적으로는 환경변수 기준으로 로드함
         *  */
        fun load(): DeploymentType {

            if (injectedDeploymentType != null) {
                log.warn { "!!!!! 주의!  강제 설정된 배포 환경이 로드됩니다 !!!!! -> $injectedDeploymentType" }
                return injectedDeploymentType!!
            }

            val config = System.getenv(DeploymentType::class.java.simpleName)
            return when {
                config.isNullOrEmpty() -> DEV
                PROD.name.equals(config, true) -> PROD
                DEV.name.equals(config, true) -> DEV
                else -> throw IllegalStateException("$config is not required DeploymentType")
            }
        }

        /** 여기 설정 있으면 우선해서 사용함 */
        private var injectedDeploymentType: DeploymentType? = null

        /**
         * 강제로 배포환경을 주입함!!!
         * 제한적으로 사용해야함
         * ex) kotlin notebook
         *  */
        fun injectForce(deploymentType: DeploymentType?) {
            log.warn { "!!!!! 주의!  배포 환경이 강제로 설정됩니다 !!!!! -> $deploymentType" }
            injectedDeploymentType = deploymentType
        }


    }

}