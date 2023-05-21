package net.kotlinx.aws.module.batchStep

import mu.KotlinLogging
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.module.batchStep.step.StepLogicRuntime
import net.kotlinx.aws.sfn.SfnUtil
import net.kotlinx.aws1.AwsClient1
import net.kotlinx.aws1.AwsInstanceTypeUtil
import net.kotlinx.aws1.s3.putObject
import net.kotlinx.core1.time.measureTimeString
import net.kotlinx.core2.calculator.ProgressInlineChecker
import net.kotlinx.core2.concurrent.coroutineExecute
import java.io.File

/**
 * S3 베이스의 설정파일
 * */
class BatchStepConfig(block: BatchStepConfig.() -> Unit) {

    lateinit var aws: AwsClient1

    /** 업로드 버킷 명 */
    lateinit var workUploadBuket: String

    /** sfn 이름 */
    lateinit var stateMachineName: String

    /** 람다 이름 */
    lateinit var lambdaFunctionName: String

    /** 최종 리포트에 사용 */
    lateinit var athenaModule: AthenaModule

    /** 업로드 인풋 경로 */
    var workUploadInputDir: String = "upload/sfnBatchModuleInput/"

    /** 업로드 아웃풋 경로 */
    var workUploadOutputDir: String = "upload/sfnBatchModuleOutput/"

    /** 로컬에서 S3업로드할 파일을 만들 작업공간. 사실 로컬 설정이라..  */
    var workDir: File = File(AwsInstanceTypeUtil.instanceType.root, "BatchStep")

    /** 커스텀 로직들 */
    val delegate: MutableMap<String, StepLogicRuntime> = mutableMapOf()

    /** 설정 등록 */
    fun <T : StepLogicRuntime> customLogicRegister(runtime: T) {
        delegate[runtime::class.simpleName!!] = runtime
    }

    init {
        block(this)
    }

    fun consoleLink(sfnId: String): String = SfnUtil.consoleLink(aws.awsConfig.awsId!!, stateMachineName, sfnId)

    /** 설정된 정보로 업로드 */
    fun upload(datas: List<String>, targetSfnId: String) {
        val thidDir = File(workDir, "${targetSfnId}}")
        val workUploadDir = "${workUploadInputDir}$targetSfnId/"
        val progressInlineChecker = ProgressInlineChecker(datas.size.toLong())
        measureTimeString {
            log.debug { "S3로 업로드 start => 데이터 ${datas.size}건" }
            thidDir.mkdirs()
            datas.mapIndexed { index, data ->
                suspend {
                    val file = File(thidDir, "$index.txt".padStart(5 + 4, '0')) //5자리까지 예상
                    file.writeText(data)
                    val workUploadKey = "${workUploadDir}${file.name}"
                    aws.s3.putObject(workUploadBuket, workUploadKey, file)
                    progressInlineChecker.check()
                }
            }.coroutineExecute(8) //6개 까지는 잘됨. 10개는 종종 오류
            thidDir.deleteRecursively() //정리
        }.also {
            log.debug { "S3로 업로드 start => 데이터 ${datas.size}건 => 걸린시간 $it" }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}


