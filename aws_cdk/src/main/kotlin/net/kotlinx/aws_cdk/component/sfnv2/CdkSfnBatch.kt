package net.kotlinx.aws_cdk.component.sfnv2

import com.lectra.koson.obj
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
     * @see net.kotlinx.aws_cdk.component.CdkBatchJobDefinition
     * */
    var input = mapOf(
        BatchUtil.BATCH_ARGS01 to obj { AwsNaming.method to name }.toString(), //첫번재 파라메터로 method 전달(로직 이름 or job 이라면 DDB PK)
        "${BatchUtil.BATCH_ARGS02}.$" to "$.${AwsNaming.option}", //두번째 파라메터로 SFN에 입력된 옵션을 넘김
    )

    override var suffix: String = ""

    override fun convert(): State {
        return BatchSubmitJob(
            cdkSfn.stack, "${name}${suffix}", BatchSubmitJobProps.builder()
                .jobName(name)
                .jobDefinitionArn(cdkSfn.jobDefinitionArn)
                .jobQueueArn(cdkSfn.jobQueueArn)
                .payload(TaskInput.fromObject(input))
                .resultPath("$.${AwsNaming.option}.${name}") //다른데서 읽을 수 있도록 option에 등록한다
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