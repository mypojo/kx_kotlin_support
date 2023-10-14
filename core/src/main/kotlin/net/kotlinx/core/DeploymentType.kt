package net.kotlinx.core

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

        /** 환경변수 기준으로 로드 */
        fun load(): DeploymentType {
            val config = System.getenv(DeploymentType::class.java.simpleName)
            return when {
                config.isNullOrEmpty() -> DEV
                PROD.name.equals(config, true) -> PROD
                DEV.name.equals(config, true) -> DEV
                else -> throw IllegalStateException("$config is not required DeploymentType")
            }
        }
    }

}