package net.kotlinx.awscdk.basic

import net.kotlinx.awscdk.CdkEnum
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ssm.ParameterDataType
import software.amazon.awscdk.services.ssm.ParameterTier
import software.amazon.awscdk.services.ssm.StringParameter
import software.amazon.awscdk.services.ssm.StringParameterProps

/** 스토어드 파라메터 */
class CdkParameter(
    val name: String,
) : CdkEnum {

    override val logicalName: String
        get() = "/cdk/${project.profileName}/${this.name}/${deploymentType.name.lowercase()}"


    /**
     * put
     * ex) fun put(stack: Stack, deploymentType: DeploymentType, value: String) = ParameterUtil.putParameter(stack, "/cdk/${this.name.lowercase()}/$deploymentType", value)
     *  */
    fun put(stack: Stack, value: String) = StringParameter(
        stack, logicalName,
        StringParameterProps.builder().parameterName(logicalName).stringValue(value).tier(ParameterTier.STANDARD).dataType(ParameterDataType.TEXT).build()
    )

    /**
     * get
     * ex) fun get(stack: Stack, deploymentType: DeploymentType): String = ParameterUtil.getParameter(stack, "/cdk/${this.name.lowercase()}/$deploymentType")
     * 참고로 StringParameter.fromStringParameterAttributes 를 사용하면 must be concrete (no Tokens) 오류가 발생한다.
     * */
    fun get(stack: Stack): String = StringParameter.valueFromLookup(stack, logicalName)


}

