package net.kotlinx.awscdk.basic

import net.kotlinx.awscdk.CdkProject
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinOrNull
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Tags
import software.constructs.IConstruct


object TagUtil {

    /** 기본 태그 삽입 */
    fun tagDefault(target: IConstruct) {
        koinOrNull<DeploymentType>()?.let { TagSet.DeploymentType.tag(target, it) }
        koinOrNull<CdkProject>()?.let { TagSet.Project.tag(target, it.projectName) }
    }

    /** 기본 배포환경 태그*/
    @Deprecated("enum 쓰세요")
    fun tag(target: IConstruct, deploymentType: DeploymentType = koin<DeploymentType>()) {
        Tags.of(target).add(deploymentType::class.java.simpleName, deploymentType.name)
    }

}