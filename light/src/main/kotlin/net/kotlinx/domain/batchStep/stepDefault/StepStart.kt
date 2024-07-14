package net.kotlinx.domain.batchStep.stepDefault

import com.lectra.koson.obj
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsInstanceMetadata
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.dispatch.LambdaDispatchLogic
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicHandler
import net.kotlinx.domain.batchStep.BatchStepConfig
import net.kotlinx.domain.batchStep.BatchStepInput
import net.kotlinx.domain.batchStep.BatchStepMode
import net.kotlinx.domain.job.JobExeFrom
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.json.serial.LocalDateTimeSerializer
import net.kotlinx.json.serial.SerialJsonSet
import net.kotlinx.json.serial.SerialParseJson
import net.kotlinx.json.serial.SerialToJson
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.regex.RegexSet
import net.kotlinx.string.retainFrom
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 시작시 메타데이터 처리
 *  */
class StepStart : LambdaDispatchLogic {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient1 by koinLazy()
    private val config: BatchStepConfig by koinLazy()
    private val jobRepository: JobRepository by koinLazy()
    private val instanceMetadata by koinLazy<AwsInstanceMetadata>()

    override suspend fun execute(input: GsonData): Any {

        val option = BatchStepInput.parseJson(input.toString()).option
        val inputDatas = config.listInputs(option.targetSfnId)

        if (inputDatas.isNotEmpty()) {
            log.warn { " ${config.workUploadBuket}/${config.workUploadInputDir}${option.targetSfnId}/  -> 데이터가 존재하지 않습니다. (retry일 경우에만 데이터가 없어도 됨)" }
        }
        log.debug { " -> [${config.workUploadBuket}/${config.workUploadInputDir}${option.targetSfnId}] -> ${inputDatas.size} 로드됨" }

        val job = net.kotlinx.domain.job.Job(option.jobPk, option.jobSk) {
            jobStatus = JobStatus.RUNNING
            reqTime = LocalDateTime.now()
            jobStatus = JobStatus.RUNNING
            ttl = DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
            instanceMetadata = this@StepStart.instanceMetadata

            //파싱값 입력 4개
            jobOption = input[AwsNaming.OPTION].str!!.toGsonData()
            jobExeFrom = JobExeFrom.SFN
            sfnId = option.sfnId
        }
        jobRepository.putItem(job)
        log.debug { "job [${job.toKeyString()}] 로그 insert" }

        return StepStartContext(
            LocalDateTime.now(),
            inputDatas.size,
            inputDatas.firstOrNull()?.substringAfterLast("/")?.retainFrom(RegexSet.NUMERIC)?.toInt() ?: 0,
            when (option.mode) {

                BatchStepMode.MAP_INLINE -> {
                    //전체 데이터 리스트를 넣어준다. -> sfn에서 읽어서 event로 전달해줌
                    inputDatas.map {
                        obj {
                            S3LogicHandler.KEY to it
                        }.toString()
                    }
                }

                BatchStepMode.LIST -> emptyList()
            }
        )

    }

}

/** 로그로 남길 기록 */
@Serializable
data class StepStartContext(
    /** 시작시간 */
    @Serializable(LocalDateTimeSerializer::class)
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
) : SerialToJson {

    override fun toJson(): String = SerialJsonSet.JSON_OTHER.encodeToString(this)

    companion object Parse : SerialParseJson {
        override fun parseJson(json: String): StepStartContext = SerialJsonSet.JSON_OTHER.decodeFromString(json)
    }

}