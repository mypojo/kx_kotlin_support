package net.kotlinx.awscdk.basic

import net.kotlinx.koin.Koins.koin
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Tags
import software.constructs.IConstruct


/**
 * 커스텀 태그 세트
 * 태그 시리즈가 언더스코어가 아니라서 이렇게 둠
 *  */
enum class TagSet {

    IamGroup,
    ;

    fun tag(target: IConstruct, value: String) {
        Tags.of(target).add(this.name, value)
    }
}

object TagUtil {

    /** 기본 배포환경 태그*/
    fun tag(target: IConstruct, deploymentType: DeploymentType = koin<DeploymentType>()) {
        Tags.of(target).add(deploymentType::class.java.simpleName, deploymentType.name)
    }

    /** 네이밍 태그. AWS 콘솔에서 이름으료 표시된다. */
    fun name(target: IConstruct, name: String) {
        Tags.of(target).add("Name", name)
    }

}