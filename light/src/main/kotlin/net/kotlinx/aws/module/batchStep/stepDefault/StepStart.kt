package net.kotlinx.aws.module.batchStep.stepDefault

import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsInfoLoader
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicHandler
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepInput
import net.kotlinx.aws.module.batchStep.BatchStepMode
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.serial.LocalDateTimeSerializer
import net.kotlinx.core.serial.SerialJsonCompanion
import net.kotlinx.core.serial.SerialJsonObj
import net.kotlinx.core.serial.SerialJsonSet
import net.kotlinx.core.string.retainFrom
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobExeFrom
import net.kotlinx.module.job.JobRepository
import net.kotlinx.module.job.JobStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 시작시 메타데이터 처리
 *  */
class StepStart : LambdaLogicHandler, KoinComponent {

    private val log = KotlinLogging.logger {}

    private val aws1: AwsClient1 by inject()
    private val config: BatchStepConfig by inject()
    private val awsInfoLoader: AwsInfoLoader by inject()
    private val jobRepository: JobRepository by inject()

    override suspend fun invoke(input: GsonData, context: Context?): Any {

        val option = BatchStepInput.parseJson(input.toString()).option
        val inputDatas = config.listInputs(option.targetSfnId)

        if(inputDatas.isNotEmpty()){
            log.warn { " ${config.workUploadBuket}/${config.workUploadInputDir}${option.targetSfnId}/  -> 데이터가 존재하지 않습니다. (retry일 경우에만 데이터가 없어도 됨)"  }
        }
        log.debug { " -> [${config.workUploadBuket}/${config.workUploadInputDir}${option.targetSfnId}] -> ${inputDatas.size} 로드됨" }

        val job = Job(option.jobPk, option.jobSk) {
            jobStatus = JobStatus.RUNNING
            reqTime = LocalDateTime.now()
            jobStatus = JobStatus.RUNNING
            ttl = DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
            awsInfo = awsInfoLoader.load()

            //파싱값 입력 4개
            jobOption = input[AwsNaming.OPTION].str
            jobExeFrom = JobExeFrom.SFN
            sfnId = option.sfnId
        }
        jobRepository.putItem(job)
        log.debug { "job [${job.toKeyString()}] 로그 insert" }

        return StepStartContext(
            LocalDateTime.now(),
            inputDatas.size,
            inputDatas.firstOrNull()?.substringAfterLast("/")?.retainFrom(RegexSet.NUMERIC)?.toInt() ?: 0 ,
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
) : SerialJsonObj {

    override fun toJson(): String = SerialJsonSet.JSON_OTHER.encodeToString(this)

    companion object : SerialJsonCompanion {
        override fun parseJson(json: String): StepStartContext = SerialJsonSet.JSON_OTHER.decodeFromString(json)
    }

}