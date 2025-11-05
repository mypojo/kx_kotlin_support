package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrock.createModelInvocationJob
import aws.sdk.kotlin.services.bedrock.getModelInvocationJob
import aws.sdk.kotlin.services.bedrock.model.*
import aws.sdk.kotlin.services.bedrockruntime.converse
import aws.sdk.kotlin.services.bedrockruntime.invokeModel
import aws.sdk.kotlin.services.bedrockruntime.model.InferenceConfiguration
import aws.sdk.kotlin.services.bedrockruntime.model.SystemContentBlock
import mu.KotlinLogging
import net.kotlinx.ai.AiModel
import net.kotlinx.ai.AiTextClient
import net.kotlinx.ai.AiTextInput
import net.kotlinx.ai.AiTextResult
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.getObjectText
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.s3.s3
import net.kotlinx.collection.doUntilTimeout
import net.kotlinx.core.Kdsl
import net.kotlinx.file.slash
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.ResultGsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.string.StringJsonUtil
import net.kotlinx.time.TimeFormat
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


/**
 * converse 가 더 추천됨.. 하지만 converse는 배치 처리가 없음
 * */
class BedrockTextClient : AiTextClient {

    @Kdsl
    constructor(block: BedrockTextClient.() -> Unit = {}) {
        apply(block)
    }

    lateinit var client: AwsClient

    /**
     * @see BedrockModels
     * */
    override lateinit var model: AiModel

    /**
     * temperature  등의 설정
     * */
    var inferenceConfig: InferenceConfiguration? = null

    /**
     * 시스템 프롬프트
     * 아래 링크 참조
     * https://docs.aws.amazon.com/bedrock/latest/userguide/advanced-prompts-templates.html
     *  */
    lateinit var systemPrompt: Any

    //==================================================== 이하 대량처리 옵션 ======================================================

    /** 배치 Role */
    lateinit var batchRole: String

    /** 입력 S3 경로 */
    lateinit var batchWorkPath: S3Data

    //==================================================== 함수 ======================================================

    /**
     * 단일 요청 버전
     * https://docs.aws.amazon.com/bedrock/latest/userguide/inference-api.html
     * */
    override suspend fun text(input: List<AiTextInput>): AiTextResult = converse(input)

    /**
     * AWS가 호출유형을 표준화한 호출방법
     * */
    suspend fun converse(inputs: List<AiTextInput>): AiTextResult {
        val resp = client.brr.converse {
            this.modelId = model.id
            this.system = listOf(
                SystemContentBlock.Text(systemPrompt.toString())  //시스템 프롬프트는 무조건 하나로
            )
            this.inferenceConfig = this@BedrockTextClient.inferenceConfig
            this.messages = listOf(
                BedrockTextConverter.convert(inputs) //무조건 한개만 입력
            )
        }
        val resultText = StringJsonUtil.cleanJsonText(resp.output!!.asMessage().content.first().asText()) //AI 모델별로 리턴이 다름
        val json = GsonData.parse(resultText)
        val inputTokens = resp.usage!!.inputTokens
        val outputTokens = resp.usage!!.outputTokens
        val result = ResultGsonData(json.isObject, json)
        return AiTextResult(model, inputs, result, inputTokens, outputTokens, resp.metrics!!.latencyMs)
    }

    /**
     * agent 아닌, 일반 모델 실행.
     * 개별 모델 전용 API임. 가능하면 통일화된 converse 를 사용할것
     * @see converse
     * */
    suspend fun invokeModel(input: List<AiTextInput>): AiTextResult {
        val json = BedrockTextInvokerConverter.convert(this, input)
        val start = System.currentTimeMillis()
        val resp = client.brr.invokeModel {
            this.modelId = model.id
            this.contentType = "application/json" //json으로 통일
            this.accept = "application/json" //json으로 통일
            this.body = json.toString().toByteArray()
        }

        val body = GsonData.parse(resp.body.toString(Charsets.UTF_8))
        val content: GsonData = body["content"]
        val inputTokens = body["usage"]["input_tokens"].int!!
        val outputTokens = body["usage"]["output_tokens"].int!!

        val duration = System.currentTimeMillis() - start
        return try {
            check(content.size == 1)
            val result = content[0]["text"].str?.let { ResultGsonData(true, it.toGsonData()) } ?: ResultGsonData(false, content)
            AiTextResult(model, input, result, inputTokens, outputTokens, duration)
        } catch (e: Exception) {
            val result = ResultGsonData(false, content)
            AiTextResult(model, input, result, inputTokens, outputTokens, duration)
        }
    }


    //==================================================== 대량처리 ======================================================

    /**
     * 간이 메소드
     * 오래걸릴 수 있으니 일케 하면 안됨
     *  */
    suspend fun invokeModelBatchAndWaitCompleted(jobName: String, datas: List<List<AiTextInput>>): List<GsonData> {
        val job = invokeModelBatch(jobName, datas)
        val results = doUntilTimeout(30.seconds, 30.minutes) { getModelBatchOrNull(job.jobArn) }
        return results
    }

    /**
     * 대량 처리 버전
     * https://docs.aws.amazon.com/bedrock/latest/userguide/batch-inference-data.html
     *  */
    suspend fun invokeModelBatch(jobName: String, datas: List<List<AiTextInput>>): CreateModelInvocationJobResponse {
        check(datas.size >= 100) { "배치는 최소 100개 이상만 가능함" }

        val jobDate = TimeFormat.YMD.get()
        val jobId = UUID.randomUUID().toString()
        val currentDir = batchWorkPath.slash(jobDate).slash(jobName).slash(jobId)

        val textClient = this
        val inputJsonl = currentDir.slash(INPUT_NAME).apply {
            //jsonl 파일 생성
            val jsonl = datas.map { BedrockTextInvokerConverter.convert(textClient, it) }.joinToString("\n")
            val inputFile = AwsInstanceTypeUtil.INSTANCE_TYPE.root.slash(jobId).slash(INPUT_NAME)
            inputFile.writeText(jsonl)
            client.s3.putObject(this, inputFile)
        }

        return client.br.createModelInvocationJob {
            this.jobName = "${jobName}-${jobDate}-${jobId}"
            this.modelId = model.id
            this.roleArn = "arn:aws:iam::${client.awsConfig.awsId}:role/${batchRole}"
            this.inputDataConfig = ModelInvocationJobInputDataConfig.S3InputDataConfig(
                ModelInvocationJobS3InputDataConfig {
                    this.s3Uri = inputJsonl.toFullPath()
                }
            )
            this.outputDataConfig = ModelInvocationJobOutputDataConfig.S3OutputDataConfig(
                ModelInvocationJobS3OutputDataConfig {
                    this.s3Uri = currentDir.toFullPathDir() //디렉토리로 입력해야함
                }
            )
            this.timeoutDurationInHours = 24 //24가 최소값임
            this.tags = listOf(
                Tag {
                    key = "jobName"
                    value = jobName
                }
            )
        }
    }

    /**
     * 배치 결과를 가져옴
     * @return 아직 처리중이면 null 리턴
     * */
    suspend fun getModelBatchOrNull(jobArn: String): List<GsonData>? {
        val current = client.br.getModelInvocationJob {
            this.jobIdentifier = jobArn
        }

        log.debug { " -> [${current.jobName}] ${current.status}" }

        if (current.status in STATUS_END) return null

        if (current.status != ModelInvocationJobStatus.Completed) throw IllegalStateException("Bedrock Batch Job Status [$jobArn] ${current.status} -> ${current.message}")

        val s3data = S3Data.parse(current.outputDataConfig!!.asS3OutputDataConfig().s3Uri)
        val resultText = client.s3.getObjectText(s3data)!!
        return resultText.split("\n").map { GsonData.parse(it) }
    }

    companion object {

        private val log = KotlinLogging.logger {}

        const val INPUT_NAME = "input.jsonl"
        const val OUTPUT_NAME = "output.jsonl"

        /** 종료된 상태 */
        val STATUS_END = listOf(
            ModelInvocationJobStatus.Completed,
            ModelInvocationJobStatus.Failed,
            ModelInvocationJobStatus.Expired,
            ModelInvocationJobStatus.Stopped,
        )
    }


}