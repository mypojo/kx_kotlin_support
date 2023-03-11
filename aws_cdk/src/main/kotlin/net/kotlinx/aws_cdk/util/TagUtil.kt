package net.kotlinx.aws_cdk.util

import software.amazon.awscdk.Tags
import software.constructs.IConstruct


object TagUtil {

    /** enum 값으로 태깅 */
    fun tag(target: IConstruct, deploymentType: Enum<*>) {
        Tags.of(target).add(deploymentType::class.java.simpleName, deploymentType.name)
    }

    /** 네이밍 태그 */
    fun name(target: IConstruct, name:String) {
        Tags.of(target).add("Name", name)
    }

}