package net.kotlinx.aws.module.batchStep

import aws.sdk.kotlin.services.sfn.startExecution
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.sfn.SfnUtil

/** SFN 실행기.  */
class BatchStepExecutor(
    val aws: AwsClient1,
    val config: BatchStepConfig
) {

    private val log = KotlinLogging.logger {}

    /**
     * @param datas 각 단위는 5~8분 이내로 처리 가능한 사이즈가 좋아보임. (부득이하게 좀 길어져도 안전하도록)
     * */
    suspend fun startExecution(datas: List<String>, block: BatchStepInput.() -> Unit = {}): BatchStepInput {
        val input = BatchStepInput().apply(block)
        if (input.retrySfnId == null) {
            config.upload(datas, input.targetSfnId)
        } else {
            log.warn { "[${input.retrySfnId}] 재시도!! S3로 업로드 스킵!!" }
        }

        aws.sfn.startExecution {
            this.input = obj {
                AwsNaming.option to rawJson(input.toJson())
            }.toString()
            this.name = input.sfnId
            this.stateMachineArn = SfnUtil.stateMachineArn(config.aws.awsConfig.awsId!!, config.stateMachineName)
        }
        return input
    }


}


