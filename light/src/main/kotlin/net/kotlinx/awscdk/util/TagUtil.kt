package net.kotlinx.awscdk.util

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

    /** 권한 체크용 태그 key */
    @Deprecated("어디쓰는지 까먹음")
    const val IAM_GROUP = "IamGroup"

    /** 기본 배포환경 태그*/
    fun tag(target: IConstruct, deploymentType: DeploymentType) {
        Tags.of(target).add(deploymentType::class.java.simpleName, deploymentType.name)
    }

    /** 네이밍 태그 */
    fun name(target: IConstruct, name: String) {
        Tags.of(target).add("Name", name)
    }

}