package net.kotlinx.aws.batchStep

import aws.sdk.kotlin.services.sfn.startExecution
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.sfn.SfnUtil
import net.kotlinx.aws1.batchStep.BatchStepConfig
import net.kotlinx.aws1.batchStep.BatchStepInput
import net.kotlinx.aws1.s3.putObject
import net.kotlinx.core1.time.measureTimeString
import net.kotlinx.core2.calculator.ProgressInlineChecker
import net.kotlinx.core2.concurrent.coroutineExecute
import java.io.File

class BatchStepExecutor(
    val aws: AwsClient,
    val batchStepConfig: BatchStepConfig
) {

    private val log = KotlinLogging.logger {}

    suspend fun startExecution(block: BatchStepInput.() -> Unit): BatchStepInput {
        val input = BatchStepInput().apply(block)

        val thidDir = File(input.workDir, "${input.sfnId}}")

        val workUploadDir = "${batchStepConfig.workUploadInputDir}${input.retrySfnId ?: input.sfnId}/"

        if (input.retrySfnId == null) {
            val progressInlineChecker = ProgressInlineChecker(input.datas.size.toLong())
            measureTimeString {
                log.info { "S3로 업로드 start => 데이터 ${input.datas.size}건" }
                thidDir.mkdirs()
                input.datas.mapIndexed { index, data ->
                    suspend {
                        val file = File(thidDir, "$index.txt".padStart(5 + 4, '0')) //5자리까지 예상
                        file.writeText(data.toString())
                        val workUploadKey = "${workUploadDir}${file.name}"
                        aws.s3.putObject(batchStepConfig.workUploadBuket, workUploadKey, file)
                        progressInlineChecker.check()
                    }
                }.coroutineExecute(8) //6개 까지는 잘됨. 10개는 종종 오류
                thidDir.deleteRecursively() //정리
            }.also {
                log.info { "S3로 업로드 start => 데이터 ${input.datas.size}건 => 걸린시간 $it" }
            }
        } else {
            log.warn { "[${input.sfnId}] 재시도!! S3로 업로드 스킵!!" }
        }

        val sfnInput = obj {
            "bucket" to batchStepConfig.workUploadBuket
            "key" to workUploadDir
        }
        aws.sfn.startExecution {
            this.input = sfnInput.toString()
            this.name = input.sfnId
            this.stateMachineArn = SfnUtil.stateMachineArn(batchStepConfig.awsId, batchStepConfig.stateMachineName)
        }
        return input
    }
}

