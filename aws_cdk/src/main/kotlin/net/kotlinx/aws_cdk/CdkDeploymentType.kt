package net.kotlinx.aws_cdk

import net.kotlinx.core1.DeploymentType


/** 배포 환경 별로 리소스가 생성되는것 */
interface CdkDeploymentType : CdkInterface {

    /** 배포 타입 (동적으로 입력)  */
    var deploymentType: DeploymentType

}

/** 간단변경 */
fun <T : CdkDeploymentType> T.deploymentType(deploymentType: DeploymentType): T {
    this.deploymentType = deploymentType
    return this
}