package net.kotlinx.domain.batchStep.stepDefault

import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceMetadata
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.dispatch.LambdaDispatchLogic
import net.kotlinx.aws.lambda.dispatch.synch.S3LogicDispatcher
import net.kotlinx.domain.batchStep.BatchStepCallback
import net.kotlinx.domain.batchStep.BatchStepConfig
import net.kotlinx.domain.batchStep.BatchStepMode
import net.kotlinx.domain.batchStep.BatchStepParameter
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobExeFrom
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.regex.RegexSet
import net.kotlinx.string.retainFrom
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 공통  처리
 *  */
class StepStart : LambdaDispatchLogic {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient by koinLazy()
    private val config: BatchStepConfig by koinLazy()
    private val jobRepository: JobRepository by koinLazy()
    private val instanceMetadata by koinLazy<AwsInstanceMetadata>()

    override suspend fun execute(input: GsonData): Any {

        val option = BatchStepParameter.parseJson(input.toString()).option

        val inputS3Paths = config.listInputs(option.targetSfnId).also {
            if (it.isEmpty()) {
                log.warn { " ${config.workUploadBuket}/${config.workUploadInputDir}${option.targetSfnId}/  -> 데이터가 존재하지 않습니다. (retry일 경우에만 데이터가 없어도 됨)" }
            } else {
                log.debug { " -> [${config.workUploadBuket}/${config.workUploadInputDir}${option.targetSfnId}] -> 파일path ${it.size}건 로드됨" }
            }
        }

        val job = Job(option.jobPk, option.jobSk) {
            jobStatus = JobStatus.RUNNING
            reqTime = LocalDateTime.now()
            jobStatus = JobStatus.RUNNING
            ttl = DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
            instanceMetadata = this@StepStart.instanceMetadata

            //파싱값 입력 4개
            //jobOption = input[AwsNaming.OPTION].str!!.toGsonData()
            jobOption = input[AwsNaming.OPTION]
            jobExeFrom = JobExeFrom.SFN
            sfnId = option.sfnId
        }
        jobRepository.putItem(job)
        log.debug { "job [${job.toKeyString()}] 로그 insert" }

        log.trace { "BatchStepCallback 이 등록되어있다면 실행" }
        Koins.koinOrNull<BatchStepCallback>(job.pk)?.let {
            it.execute(option, job)
        }

        return StepStartContext(
            LocalDateTime.now(),
            inputS3Paths.size,
            inputS3Paths.firstOrNull()?.substringAfterLast("/")?.retainFrom(RegexSet.NUMERIC)?.toInt() ?: 0,
            when (option.mode) {

                BatchStepMode.MAP_INLINE -> {
                    //전체 데이터 리스트를 넣어준다. -> sfn에서 읽어서 event로 전달해줌
                    inputS3Paths.map {
                        obj {
                            S3LogicDispatcher.KEY to it
                        }.toString()
                    }
                }

                BatchStepMode.LIST -> emptyList()
            }
        )

    }

}

/** 로그로 남길 기록 */
data class StepStartContext(
    /** 시작시간 */
    val startTime: LocalDateTime,
    /** 이번로드 전체수 */
    val total: Int,
    /** 이번로드 첫 파일 넘버 */
    val first: Int,
    /**
     * MAP인경우 전체 데이터의 S3 key (SFN에서 이 경로로 읽어감)
     * array<String> 이며 문자는 json 형식이어야함  {Key:...}
     *  */
    val datas: List<String>,
)