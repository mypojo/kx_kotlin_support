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
import net.kotlinx.domain.job.Job
import net.kotlinx.json.gson.toGsonDataOrEmpty
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.time.TimeStart
import net.kotlinx.time.measureTimeString
import net.kotlinx.time.toTimeString
import java.io.File
import java.util.*
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

    private val aws: AwsClient by koinLazy()
    private val config: BatchStepConfig by koinLazy()

    /** 로컬에서 결과 체크하는 주기 */
    var checkInterval: Duration = 30.seconds

    /** 로컬에서 최대 체크 숫자 */
    var checkLimit: Int = 200

    /** 로컬에서 S3업로드할 파일을 만들 작업공간. 사실 로컬 설정이라..  */
    var workDir: File = File(AwsInstanceTypeUtil.INSTANCE_TYPE.root, "BatchStep")

    /**
     * 로컬에서 SFN 종료되면 출력해줄 후크
     * ex) 정상종료 확인용 작업 결과치 간단 통계
     *  */
    var localCallback: ((Job) -> Unit)? = null

    /**
     * 작업들 업로드 & SFN실행 한번에.
     * @param datas 각 단위는 5~8분 이내로 처리 가능한 사이즈가 좋아보임. (부득이하게 좀 길어져도 안전하도록)
     * */
    suspend fun startExecution(parameter: BatchStepParameter, datas: List<S3LogicInput>): BatchStepParameter {
        val option = parameter.option
        if (option.retrySfnId == null) {
            upload(datas, option.targetSfnId)
        } else {
            log.warn { "[${option.retrySfnId}] 재시도 요청 -> S3로 업로드는 스킵!!" }
        }
        aws.sfn.startExecution(config.stateMachineName, parameter.option.sfnId, parameter.toJson())
        return parameter
    }

    /**
     * 설정된 정보로 업로드
     * 파일 업로드는 프로그레스 체크할정도로 오래걸리지 않음
     *  */
    fun upload(datas: List<S3LogicInput>, targetSfnId: String) {
        val thidDir = File(workDir, "${targetSfnId}}")
        val workUploadDir = "${config.workUploadInputDir}$targetSfnId/"
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
                }
            }.coroutineExecute(8) //6개 까지는 잘됨. 10개는 종종 오류
            thidDir.deleteRecursively() //정리
        }.also {
            log.debug { "S3로 업로드 start => 데이터 ${datas.size}건 => 걸린시간 $it" }
        }
    }

    /**
     * 실행 간단 축약버전
     * 내부 프로그램은 이걸로 통일하자.
     * @param block 여기서 mode 등을 조절하면됨
     *  */
    suspend fun startExecution(pk: String, sk: String, splitedDatas: List<List<String>>, inputOption: Any, block: BatchStepOption.() -> Unit = {}): Job {

        val inputOptionJson = inputOption.toString()
        val s3LogicInputs = splitedDatas.map {
            S3LogicInput(pk, it, inputOptionJson)
        }
        val jobParam = Job(pk, sk)
        //UUID를 꼭 쓰지 않아도 됨. 파일이름으로 혀용되는 이름으로 달것!
        val sfnUuid = "${jobParam.pk}-${jobParam.sk}"
        jobParam.sfnId = sfnUuid
        val parameter = BatchStepParameter {
            jobPk = jobParam.pk
            jobSk = jobParam.sk
            sfnId = sfnUuid
            block()
        }

        startExecution(parameter, s3LogicInputs)

        if (AwsInstanceTypeUtil.IS_LOCAL) {
            log.trace { "로컬인경우 작업을 다 기다린다음 결과 출력" }
            waitResult(sfnUuid)
            checkResult(sfnUuid)
            localCallback?.let { it.invoke(jobParam) }
        }
        return jobParam
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


    /** 간단 재실행 */
    suspend fun retry(failedSfnId: String) {

        val batchStepConfig: BatchStepConfig = koin()

        val execution = batchStepConfig.describeExecution(failedSfnId)
        log.warn { "작업 재시도!! [$failedSfnId] -> 작업상태 ${execution.status}" }
        check(execution.status == ExecutionStatus.Failed) { "작업상태가 실패 상태가 아닙니다!!" }

        val option = execution.input.toGsonDataOrEmpty()["option"]
        log.info { "재시도 입력값 : $option" }

        val sfnUuid = UUID.randomUUID().toString()
        val input = BatchStepParameter {
            jobPk = option[AwsNaming.JOB_PK].str!!
            jobSk = option[AwsNaming.JOB_SK].str!!
            sfnId = sfnUuid
            retrySfnId = failedSfnId
            option["sfnOption"].lett {
                sfnOption = it.str!!  //단순 문자열임 주의!!
            }
        }
        startExecution(input, emptyList())
        waitResult(sfnUuid)
        checkResult(sfnUuid)
    }


}


