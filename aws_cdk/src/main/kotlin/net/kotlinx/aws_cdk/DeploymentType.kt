package net.kotlinx.aws_cdk

/**
 * AWS 실제 인프라가 구성되는 배포 타입.
 * 샘플로 사용
 */
enum class DeploymentType {
    /**
     * 운영서버
     */
    prod,

    /** 개발서버  */
    dev
}