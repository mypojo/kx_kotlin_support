package net.kotlinx.aws_cdk.util

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ssm.ParameterDataType
import software.amazon.awscdk.services.ssm.ParameterTier
import software.amazon.awscdk.services.ssm.StringParameter
import software.amazon.awscdk.services.ssm.StringParameterProps


/**
 * AWS 파라메터 스토어
 * 각종 인프라들을 name으로 조회해도 되긴 한데, 삭제이후 캐시 등 여러 오류가 발생할 수 있다. 따라서 ID로 명시한후 작업할것
 *  */
object ParameterUtil {

    /**
     * put
     * ex) fun put(stack: Stack, deploymentType: DeploymentType, value: String) = ParameterUtil.putParameter(stack, "/cdk/${this.name.lowercase()}/$deploymentType", value)
     *  */
    fun putParameter(stack: Stack, paramId: String, value: String) {
        StringParameter(
            stack, paramId,
            StringParameterProps.builder().parameterName(paramId).stringValue(value).tier(ParameterTier.STANDARD).dataType(ParameterDataType.TEXT).build()
        )

    }

    /**
     * get
     * ex) fun get(stack: Stack, deploymentType: DeploymentType): String = ParameterUtil.getParameter(stack, "/cdk/${this.name.lowercase()}/$deploymentType")
     * 참고로 StringParameter.fromStringParameterAttributes 를 사용하면 must be concrete (no Tokens) 오류가 발생한다.
     * */
    fun getParameter(stack: Stack, paramId: String): String = StringParameter.valueFromLookup(stack, paramId)

//    return StringParameter.fromStringParameterAttributes(
//    stack, paramId,
//    StringParameterAttributes.builder().parameterName(paramId).build()
//    ).stringValue


}