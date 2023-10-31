package net.kotlinx.aws.module.batchStep

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicInput
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.sfn.startExecution
import net.kotlinx.core.calculator.ProgressInlineChecker
import net.kotlinx.core.concurrent.coroutineExecute
import net.kotlinx.core.time.measureTimeString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * SFN 실행기.
 * 리스트 기억해서, 일단 전부 실행 후, 안된거 파악하게 수정하기
 *  */
class BatchStepExecutor : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient1 by inject()
    private val config: BatchStepConfig by inject()

    /** 로컬에서 S3업로드할 파일을 만들 작업공간. 사실 로컬 설정이라..  */
    var workDir: File = File(AwsInstanceTypeUtil.INSTANCE_TYPE.root, "BatchStep")

    /**
     * 작업들 업로드 & SFN실행 한번에.
     * @param datas 각 단위는 5~8분 이내로 처리 가능한 사이즈가 좋아보임. (부득이하게 좀 길어져도 안전하도록)
     * */
    @BatchStepInputDsl
    suspend fun startExecution(datas: List<S3LogicInput>, block: BatchStepInput.() -> Unit = {}): BatchStepInput {

        val input = BatchStepInput(block)
        val option = input.option
        if (option.retrySfnId == null) {
            upload(datas, option.targetSfnId)
        } else {
            log.warn { "[${option.retrySfnId}] 재시도 요청 -> S3로 업로드는 스킵!!" }
        }

        aws.sfn.startExecution(aws.awsConfig.awsId!!, config.stateMachineName, input.option.sfnId, input.toJson())

        return input
    }

    /** 설정된 정보로 업로드 */
    suspend fun upload(datas: List<S3LogicInput>, targetSfnId: String) {
        val thidDir = File(workDir, "${targetSfnId}}")
        val workUploadDir = "${config.workUploadInputDir}$targetSfnId/"
        val progressInlineChecker = ProgressInlineChecker(datas.size.toLong())
        measureTimeString {
            log.debug { "S3로 업로드 start => 데이터 ${datas.size}건" }
            thidDir.mkdirs()
            datas.mapIndexed { index, data ->
                suspend {
                    val file = File(thidDir, "$index.txt".padStart(5 + 4, '0')) //5자리까지 예상
                    val textJson = data.toJson()
                    file.writeText(textJson)
                    val workUploadKey = "${workUploadDir}${file.name}"
                    aws.s3.putObject(config.workUploadBuket, workUploadKey, file)
                    progressInlineChecker.check()
                }
            }.coroutineExecute(8) //6개 까지는 잘됨. 10개는 종종 오류
            thidDir.deleteRecursively() //정리
        }.also {
            log.debug { "S3로 업로드 start => 데이터 ${datas.size}건 => 걸린시간 $it" }
        }
    }


}


