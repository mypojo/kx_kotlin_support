package net.kotlinx.awscdk.sfn

import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.batch.BatchUtil
import software.amazon.awscdk.services.stepfunctions.State
import software.amazon.awscdk.services.stepfunctions.TaskInput
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJob
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJobProps

class CdkSfnBatch(
    override val cdkSfn: CdkSfn,
    override val name: String,
) : CdkSfnChain {

    /**
     * 정해진 키 값들이 args[] 로 매핑됨. -> 각 데이터는 문자열 형식만 가능함
     * 여기 키값 들은 batch 에서 매핑을 따로 해주어야 한다.
     * */
    var input = mapOf(
        BatchUtil.BATCH_ARGS01 to "$.${AwsNaming.OPTION}" //이거 하나로
    )

    override var suffix: String = ""

    override fun convert(): State {
        return BatchSubmitJob(
            cdkSfn.stack, "${name}${suffix}", BatchSubmitJobProps.builder()
                .jobName(name)
                .jobDefinitionArn(cdkSfn.jobDefinitionArn)
                .jobQueueArn(cdkSfn.jobQueueArn)
                .payload(TaskInput.fromObject(input))
                .resultPath("$.${AwsNaming.OPTION}.${name}") //다른데서 읽을 수 있도록 option에 등록한다
                .resultSelector(
                    mapOf(
                        "jobName" to name,
                        "statusCode.$" to "$.Status",
                        "StatusReason.$" to "$.StatusReason", //배치 잡의 경우 여러가지 상태가 필요함.
                    )
                )
                .comment(name)
                .build()
        )
    }
}