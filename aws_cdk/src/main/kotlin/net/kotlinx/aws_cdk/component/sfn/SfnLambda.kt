package net.kotlinx.aws_cdk.component.sfn

import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws.sfn.SfnUtil
import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.TaskInput
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvokeProps

data class SfnLambda(
    override val name: String,
    override var suffix: String = ""
) : SfnChain {
    /** 람다를 내부적으로 구분할때 사용하는 네이밍.  기본적으로는 job 으로 간주 */
    var keyName = BatchUtil.JOB_PK

    /** 기본 재시도 정책(10초 3회). 람다 특성상, 혹시 모르니 리트라이 해준다. 커스텀은 addRetry() 사용 */
    var retry: Boolean = true

    override fun convert(cdkSfn: CdkSfn): State {
        return LambdaInvoke(
            cdkSfn.stack, "${name}${suffix}", LambdaInvokeProps.builder()
                .lambdaFunction(cdkSfn.lambda)
                .payload(
                    TaskInput.fromObject(
                        mapOf(
                            keyName to name, //실행할 잡 이름 (설정과 일치해야함)
                            "${SfnUtil.jobOption}.$" to "$.${SfnUtil.jobOption}",  //기준날짜 등의 커스텀 옵션 (포함된 모든 잡이 같이 사용)
                        )
                    )
                )
                .resultPath("\$.${name}-result")
                .resultSelector(
                    mapOf(
                        "jobName" to name,
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