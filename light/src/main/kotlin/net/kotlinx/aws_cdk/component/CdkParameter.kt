package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkEnum
import net.kotlinx.aws_cdk.util.ParameterUtil
import software.amazon.awscdk.Stack

/** enum */
class CdkParameter(
    val name: String,
) : CdkEnum {

    override val logicalName: String
        get() = "/cdk/${project.projectName}/${this.name}/${deploymentType.name.lowercase()}"

    fun put(stack: Stack, value: String) = ParameterUtil.putParameter(stack, logicalName, value)
    fun get(stack: Stack): String = ParameterUtil.getParameter(stack, logicalName)


}

