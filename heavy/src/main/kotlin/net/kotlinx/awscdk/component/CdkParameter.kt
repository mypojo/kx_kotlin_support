package net.kotlinx.awscdk.component

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.util.ParameterUtil
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

