package net.kotlinx.awscdk.sfn2

import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.batch.BatchUtil
import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.TaskInput
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJob
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJobProps

class CdkSfnBatch(
    override val sfn: CdkSfn,
    override val id: String,
    override val stateName: String,
) : CdkSfnChain {

    /**
     * 입력 패스
     * BATCH 의 arg[] 로 들어가는데, 여기는 단순 문자열만 입력 가능함 -> 이때문에 직렬화 해줌
     * https://docs.aws.amazon.com/step-functions/latest/dg/intrinsic-functions.html 참고
     * 특수문자가 들어가면 안되는경우가 많으니 최대한 간한하게 작업할것
     *  */
    var inputPath: String = "States.JsonToString($.${AwsNaming.OPTION})"

    override fun convert(): State {

        val input = mapOf(
            "${BatchUtil.BATCH_ARGS01}.$" to inputPath,
            "${BatchUtil.BATCH_ARGS02}" to id,
        )

        return BatchSubmitJob(
            sfn.stack, "${sfn.logicalName}-${id}", BatchSubmitJobProps.builder()
                .jobName(id)
                .jobDefinitionArn(sfn.jobDefinitionArn)
                .jobQueueArn(sfn.jobQueueArn)
                .payload(TaskInput.fromObject(input))
                .resultPath("$.${AwsNaming.OPTION}.${id}") //다른데서 읽을 수 있도록 option에 등록한다
                .resultSelector(
                    mapOf(
                        "jobName" to id,
                        "statusCode.$" to "$.Status",
                        "StatusReason.$" to "$.StatusReason", //배치 잡의 경우 여러가지 상태가 필요함.
                    )
                )
                .comment(id)
                .stateName(stateName)
                .build()
        )
    }
}