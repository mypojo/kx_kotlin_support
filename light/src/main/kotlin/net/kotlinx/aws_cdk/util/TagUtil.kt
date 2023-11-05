package net.kotlinx.aws_cdk.util

import software.amazon.awscdk.Tags
import software.constructs.IConstruct


/** 커스텀 태그 세트 */
enum class TagSet {

    IamGroup,
    ;

    fun tag(target: IConstruct, value: String) {
        Tags.of(target).add(this.name, value)
    }
}

object TagUtil {

    /** 권한 체크용 태그 key */
    const val IAM_GROUP = "IamGroup"


    /** enum 값으로 태깅 */
    fun tag(target: IConstruct, deploymentType: Enum<*>) {
        Tags.of(target).add(deploymentType::class.java.simpleName, deploymentType.name)
    }

    /** 네이밍 태그 */
    fun name(target: IConstruct, name: String) {
        Tags.of(target).add("Name", name)
    }

}