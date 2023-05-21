package net.kotlinx.aws_cdk.component.sfnv2

import net.kotlinx.aws1.AwsNaming
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.stepfunctions.TaskInput
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvokeProps

class CdkSfnLambda(
    override val cdkSfn: CdkSfn,
    override val name: String,
) : CdkSfnChain {

    override var suffix: String = ""

    /** 오버라이드 */
    var lambda: IFunction = cdkSfn.lambda

    /** 기본 재시도 정책(10초 3회). 람다 특성상, 혹시 모르니 리트라이 해준다. 커스텀은 addRetry() 사용 */
    var retry: Boolean = true

    override fun convert(): LambdaInvoke {
        return LambdaInvoke(
            cdkSfn.stack, "${name}${suffix}", LambdaInvokeProps.builder()
                .lambdaFunction(lambda)
                .payload(
                    TaskInput.fromObject(
                        mapOf(
                            AwsNaming.method to name,
                            "${AwsNaming.option}.$" to "$.${AwsNaming.option}",  //기준날짜 등의 커스텀 옵션
                        )
                    )
                )
                .resultPath("$.${AwsNaming.option}.${name}") //다른데서 읽을 수 있도록 option에 등록한다
                .resultSelector(
                    mapOf(
                        //AwsNaming.method to name,
                        //"statusCode.$" to "$.StatusCode",
                        "body.$" to "$.Payload", //실무에서는 이거만 씀. 혹시 모르니 뎁스 구조는 남겨놓음
                    )
                )
                .comment(name)
                .retryOnServiceExceptions(retry)
                .build()
        )
    }
}