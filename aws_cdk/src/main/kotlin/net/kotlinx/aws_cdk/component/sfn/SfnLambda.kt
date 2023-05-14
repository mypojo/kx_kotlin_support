package net.kotlinx.aws_cdk.component.sfn

import net.kotlinx.aws1.AwsNaming
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.TaskInput
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvokeProps

class SfnLambda(
    override val name: String,
    override var suffix: String = ""
) : SfnChain {

    /** 오버라이드 */
    var lambdaFunction: IFunction? = null

    /** 기본 재시도 정책(10초 3회). 람다 특성상, 혹시 모르니 리트라이 해준다. 커스텀은 addRetry() 사용 */
    var retry: Boolean = true

    override fun convert(cdkSfn: CdkSfn): State {
        return LambdaInvoke(
            cdkSfn.stack, "${name}${suffix}", LambdaInvokeProps.builder()
                .lambdaFunction(lambdaFunction ?: cdkSfn.lambda)
                .payload(
                    TaskInput.fromObject(
                        mapOf(
                            AwsNaming.method to name,
                            "${AwsNaming.option}.$" to "$.${AwsNaming.option}",  //기준날짜 등의 커스텀 옵션
                        )
                    )
                )
                .resultPath("\$.${name}-result")
                .resultSelector(
                    mapOf(
                        AwsNaming.method to name,
                        "statusCode.$" to "$.StatusCode",
                        "body.$" to "$.Payload",
                    )
                )
                .comment("$name")
                .retryOnServiceExceptions(retry)
                .build()
        )
    }
}