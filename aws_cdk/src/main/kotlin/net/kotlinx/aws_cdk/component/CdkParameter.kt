package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.ParameterUtil
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack

/** enum */
class CdkParameter(
    val project: CdkProject,
    val name: String,
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = DeploymentType.dev

    override val logicalName: String
        get() = "/cdk/${project.projectName}/${this.name}/$deploymentType"

    fun put(stack: Stack, value: String) = ParameterUtil.putParameter(stack, logicalName, value)
    fun get(stack: Stack): String = ParameterUtil.getParameter(stack, logicalName)


}

