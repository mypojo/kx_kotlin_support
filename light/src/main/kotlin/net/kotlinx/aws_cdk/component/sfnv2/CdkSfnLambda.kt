package net.kotlinx.aws_cdk.component.sfnv2

import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws_cdk.toCdk
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.stepfunctions.RetryProps
import software.amazon.awscdk.services.stepfunctions.TaskInput
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvokeProps
import kotlin.time.Duration

class CdkSfnLambda(
    override val cdkSfn: CdkSfn,
    /** decapital 하게 넣자. */
    override val name: String,
) : CdkSfnChain {

    override var suffix: String = ""

    /** 오버라이드 */
    var lambda: IFunction = cdkSfn.lambda

    /**
     * 기본 AWS 기능에 대한 재시도 정책(10초 3회)
     * ex) ServiceException, TooManyRequestsException , SdkClientException 등등
     * 보통 재시도에 안전하게 만들고 이걸 켜야함
     * */
    var serviceRetry: Boolean = true

    /**
     * 모든 실패에 대한 리트라이를 할지 여부
     * ex) 15분 초과 타임아웃 -> 가끔 이유없이 남
     * <MaxAttempts,interval>
     * */
    var allRetry: Pair<Number, Duration>? = null

    override fun convert(): LambdaInvoke {
        val lambdaInvoke = LambdaInvoke(
            cdkSfn.stack, "${name}${suffix}", LambdaInvokeProps.builder()
                .lambdaFunction(lambda)
                .payload(
                    TaskInput.fromObject(
                        mapOf(
                            AwsNaming.METHOD to name,
                            "${AwsNaming.OPTION}.$" to "$.${AwsNaming.OPTION}",  //기준날짜 등의 커스텀 옵션
                        )
                    )
                )
                .resultPath("$.${AwsNaming.OPTION}.${name}") //다른데서 읽을 수 있도록 option에 등록한다
                .resultSelector(
                    mapOf(
                        //AwsNaming.method to name,
                        //"statusCode.$" to "$.StatusCode",
                        "body.$" to "$.Payload", //실무에서는 이거만 씀. 혹시 모르니 뎁스 구조는 남겨놓음
                    )
                )
                .comment(name)
                .retryOnServiceExceptions(serviceRetry)
                .build()
        )
        allRetry?.let {
            lambdaInvoke.addRetry(
                RetryProps.builder()
                    .errors(
                        listOf("States.ALL")
                    )
                    .maxAttempts(it.first.toInt())
                    .interval(it.second.toCdk())
                    .backoffRate(2)
                    .build()
            )
        }
        return lambdaInvoke
    }
}