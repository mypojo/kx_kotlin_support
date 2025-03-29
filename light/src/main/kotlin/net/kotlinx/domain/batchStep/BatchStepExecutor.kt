package net.kotlinx.domain.batchStep

import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import aws.smithy.kotlin.runtime.time.epochMilliseconds
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.s3.s3
import net.kotlinx.aws.sfn.sfn
import net.kotlinx.aws.sfn.startExecution
import net.kotlinx.collection.repeatUntil
import net.kotlinx.concurrent.CoroutineSleepTool
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.concurrent.delay
import net.kotlinx.core.Kdsl
import net.kotlinx.file.gzip
import net.kotlinx.file.slashDir
import net.kotlinx.json.gson.toGsonDataOrEmpty
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.time.TimeStart
import net.kotlinx.time.measureTimeString
import net.kotlinx.time.toTimeString
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * SFN 실행기.
 * 리스트 기억해서, 일단 전부 실행 후, 안된거 파악하게 수정하기
 *  */
class BatchStepExecutor {

    @Kdsl
    constructor(block: BatchStepExecutor.() -> Unit = {}) {
        apply(block)
    }

    private val log = KotlinLogging.logger {}

    /** 클라이언트 */
    private val aws: AwsClient by koinLazy()

    /** 설정 */
    private val config: BatchStepConfig by koinLazy()

    /** 로컬에서 결과 체크하는 주기 */
    var checkInterval: Duration = 30.seconds

    /** 로컬에서 최대 체크 숫자 */
    var checkLimit: Int = 200

    /** 로컬에서 S3업로드할 파일을 만들 작업공간. 사실 로컬 설정이라..  */
    var workDir: File = File(AwsInstanceTypeUtil.INSTANCE_TYPE.root, "BatchStep")

    /**
     * 실생시 SFN을 동기화 해서 기다릴지??
     * 보통 로컬에서 디버깅할때만 이렇게 사용함
     *  */
    var synchSfn = AwsInstanceTypeUtil.IS_LOCAL

    /**
     * 처리할 데이터를 인메모리로 업로드
     * 파일 업로드는 프로그레스 체크할정도로 오래걸리지 않음
     * @param datas 각 단위는 5~8분 이내로 처리 가능한 사이즈가 좋아보임. (부득이하게 좀 길어져도 안전하도록)
     *  */
    fun uploadAllInmemory(targetSfnId: String, datas: List<S3LogicInput>) {
        val thisDir = workDir.slashDir(targetSfnId)
        measureTimeString {
            log.debug { "S3로 업로드 start => 데이터 ${datas.size}건" }
            datas.mapIndexed { index, data -> suspend { writeAndUpload(thisDir, index, data, targetSfnId) } }.coroutineExecute(8) //6개 까지는 잘됨. 10개는 종종 오류
            thisDir.deleteRecursively() //정리
        }.also {
            log.debug { "S3로 업로드 start => 데이터 ${datas.size}건 => 걸린시간 $it" }
        }
    }

    /** 하나씩 업로드 */
    suspend fun upload(targetSfnId: String, index: Int, data: S3LogicInput) {
        check(index >= 0)
        val thisDir = workDir.slashDir(targetSfnId)
        writeAndUpload(thisDir, index, data, targetSfnId)
    }

    private suspend fun writeAndUpload(thisDir: File, index: Int, data: S3LogicInput, targetSfnId: String) {
        val file = File(thisDir, "$index.txt".padStart(5 + 4, '0')).apply {
            //파일 인련번호는 5자리까지 예상
            val textJson = data.toJson()
            writeText(textJson)
        }.gzip()
        val workUploadKey = "${config.workUploadInputDir}$targetSfnId/${file.name}"
        aws.s3.putObject(config.workUploadBuket, workUploadKey, file)
    }

    /**
     * Job 베이스의 간단 실행
     * 결과 대기 & 콜백 기능이 추가됨
     *  */
    suspend fun startExecution(parameter: BatchStepParameter) {

        val sfnId = parameter.option.sfnId
        aws.sfn.startExecution(config.stateMachineName, parameter.option.sfnId, parameter.toJson()) //이 메소드를 여기서만 호출함

        if (synchSfn) {
            log.trace { "로컬인경우 작업을 다 기다린다음 결과 출력" }
            waitResult(sfnId)
            checkResult(sfnId)
        }
    }

    /**
     * 간단 재실행
     * 그대로 재실행함으로 별도의 옵션 필요없음
     *  */
    suspend fun startExecutionRetry(failedSfnId: String, newSfnId: String) {

        val execution = config.describeExecution(failedSfnId)
        log.warn { "작업 재시도!! [$failedSfnId] -> job의 상태 ${execution.status}" }
        check(execution.status == ExecutionStatus.Failed) { "SFN은 작업상태가 ${ExecutionStatus.Failed} 인것만 재시도가 가능합니다" }

        val optionBody = execution.input.toGsonDataOrEmpty()["option"]
        log.info { "재시도 입력값 : $optionBody" }

        val parameter = BatchStepParameter {
            option = BatchStepOption {
                jobPk = optionBody[AwsNaming.JOB_PK].str!!
                jobSk = optionBody[AwsNaming.JOB_SK].str!!
                sfnId = newSfnId
                retrySfnId = failedSfnId
                optionBody["sfnOption"].lett {
                    sfnOption = it.str!!  //단순 문자열임 주의!!
                }
            }

        }

        startExecution(parameter)
    }


    /** SFN이 종료될때까지 대기한다 */
    suspend fun waitResult(sfnId: String) {
        val batchStepConfig: BatchStepConfig = koin()

        val consoleLink = batchStepConfig.consoleLink(sfnId)
        log.info { "SFN 실행됨 $consoleLink" }

        val timeStart = TimeStart()

        val sleepTool = CoroutineSleepTool(checkInterval)
        sleepTool.checkAndSleep()

        repeatUntil(checkLimit) {
            sleepTool.checkAndSleep()
            val execution = batchStepConfig.describeExecution(sfnId)
            log.debug { " -> 작업상태 ${execution.status} / $timeStart ..." }
            execution.status != ExecutionStatus.Running
        }
    }


    /** 과거 날짜를 체크할수도 있음 */
    suspend fun checkResult(sfnId: String) {
        log.trace { "로우데이터 조회 샘플 : SELECT * FROM batch_step where sfn_id = '${sfnId}' limit 10 " }
        2.seconds.delay() //일정시간 기다려야함

        val execution = koin<BatchStepConfig>().describeExecution(sfnId)
        val option = execution.output.toGsonDataOrEmpty()["option"]
        log.warn { "SFN 결과 출력" }
        try {
            val resultJson = option["stepEnd"]["body"]
            resultJson.entryMap().forEach { println(" -> $it") }

            val interval = (execution.stopDate?.epochMilliseconds ?: System.currentTimeMillis()) - execution.startDate.epochMilliseconds
            log.info { "전체 걸린시간 : ${interval.toTimeString()}" }

            val totalCnt = resultJson["데이터크기"].str!!.substringBefore("건").toDouble()
            log.info { "100건 처리당 걸린시간 : ${(interval * 100.0 / totalCnt).toLong().toTimeString()}" }
        } catch (e: Exception) {
            log.warn { "결과로그 파싱 실패!! -> ${option.toPreety()}" }
        }

    }


}


