package net.kotlinx.aws_cdk.component.sfn

import com.lectra.koson.obj
import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws.sfn.SfnUtil
import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.TaskInput
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJob
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJobProps

class SfnBatch(
    override val name: String,
    override var suffix: String = ""
) : SfnChain {
    override fun convert(cdkSfn: CdkSfn): State {
        return BatchSubmitJob(
            cdkSfn.stack, "${name}${suffix}", BatchSubmitJobProps.builder()
                .jobName(name)
                .jobDefinitionArn(cdkSfn.jobDefinitionArn)
                .jobQueueArn(cdkSfn.jobQueueArn)
                .payload(
                    TaskInput.fromObject(
                        //정해진 키 값들이 args[] 로 매핑됨. -> 각 데이터는 문자열 형식만 가능함
                        mapOf(
                            BatchUtil.BATCH_ARGS01 to obj { BatchUtil.JOB_PK to name }.toString(),
                            "${BatchUtil.BATCH_ARGS02}.$" to "$.${SfnUtil.jobOption}",
                        )
                    )
                )
                .resultPath("$.${name}-result")
                .resultSelector(
                    mapOf(
                        "jobName" to name,
                        "statusCode.$" to "$.Status",
                        "StatusReason.$" to "$.StatusReason",
                    )
                )
                .comment("$name")
                .build()
        )
    }
}