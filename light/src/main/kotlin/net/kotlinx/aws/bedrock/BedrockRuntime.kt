package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrock.createModelInvocationJob
import aws.sdk.kotlin.services.bedrock.getModelInvocationJob
import aws.sdk.kotlin.services.bedrock.model.*
import com.lectra.koson.KosonType
import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import io.ktor.util.*
import mu.KotlinLogging
import net.kotlinx.ai.AiModel
import net.kotlinx.ai.AiTextClient
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
import net.kotlinx.json.koson.toKsonArray
import net.kotlinx.time.TimeFormat
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


class BedrockRuntime : AiTextClient {

    @Kdsl
    constructor(block: BedrockRuntime.() -> Unit = {}) {
        apply(block)
    }

    lateinit var client: AwsClient

    /**
     * @see BedrockModels
     * */
    override lateinit var model: AiModel

    /** 결과토큰인가? */
    var maxTokens: Int = 1000

    /**
     * 0~1
     * 0일수록 보수적인 답변. 검수 같은 작업은 0으로 세팅
     * */
    var temperature: Int = 0

    /**
     * 0~1
     * ?? 모름.  검수 작업에는 0.1을 추천한다고 하네?
     *  */
    var topP: Double = 0.1

    /**
     * 결과를 몇개 고를지
     * 일반적으로 1개면 될듯
     *  */
    var topK: Int = 1

    /**
     * 시스템 프롬프트
     * 아래 링크 참조
     * https://docs.aws.amazon.com/bedrock/latest/userguide/advanced-prompts-templates.html
     *  */
    lateinit var system: Any

    //==================================================== 이하 대량처리 옵션 ======================================================

    /** 배치 Role */
    lateinit var batchRole: String

    /** 입력 S3 경로 */
    lateinit var workPath: S3Data

    override suspend fun chat(msg: String): AiTextResult = invokeModel(listOf(msg))

    /**
     * 단일 요청 버전
     * https://docs.aws.amazon.com/bedrock/latest/userguide/inference-api.html
     * */
    suspend fun invokeModel(messages: List<Any>): AiTextResult {
        val data = createJson(messages)
        return client.brr.invokeModel(model, data)
    }

    private fun createJson(messages: List<Any>): ObjectType {
        val data = obj {
            "anthropic_version" to "bedrock-2023-05-31"
            "max_tokens" to maxTokens
            "temperature" to temperature
            "top_p" to topP
            "top_k" to topK
            "system" to system.toString()
            "messages" to arr[
                obj {
                    "role" to "user"
                    "content" to messages.map { convert(it) }.toKsonArray()
                },
            ]
        }
        return data
    }

    /**
     * 간이 메소드
     * 오래걸릴 수 있으니 일케 하면 안됨
     *  */
    suspend fun invokeModelBatchAndWaitCompleted(jobName: String, datas: List<List<Any>>): List<GsonData> {
        val job = invokeModelBatch(jobName, datas)
        val results = doUntilTimeout(30.seconds, 30.minutes) { getModelBatchOrNull(job.jobArn) }
        return results
    }

    /**
     * 대량 처리 버전
     * https://docs.aws.amazon.com/bedrock/latest/userguide/batch-inference-data.html
     *  */
    suspend fun invokeModelBatch(jobName: String, datas: List<List<Any>>): CreateModelInvocationJobResponse {
        check(datas.size >= 100) { "배치는 최소 100개 이상만 가능함" }

        val jobDate = TimeFormat.YMD.get()
        val jobId = UUID.randomUUID().toString()
        val currentDir = workPath.slash(jobDate).slash(jobName).slash(jobId)

        val inputJsonl = currentDir.slash(INPUT_NAME).apply {
            //jsonl 파일 생성
            val jsonl = datas.map { createJson(it) }.joinToString("\n")
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

        private fun convert(any: Any): KosonType {
            return when (any) {
                is File -> {
                    obj {
                        "type" to "image"
                        "source" to obj {
                            "type" to "base64" //지금은 base64 만 지원하는듯. s3 안됨
                            "media_type" to "image/${any.extension.lowercase()}"
                            "data" to any.readBytes().encodeBase64()
                        }
                    }
                }

                else -> {
                    obj {
                        "type" to "text"  //'text', 'image', 'tool_use', 'tool_result'
                        "text" to any.toString()
                    }
                }
            }
        }
    }


}