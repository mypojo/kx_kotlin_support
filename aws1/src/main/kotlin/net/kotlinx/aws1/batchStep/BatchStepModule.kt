package net.kotlinx.aws1.batchStep

import aws.sdk.kotlin.services.s3.deleteObject
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import mu.KotlinLogging
import net.kotlinx.aws1.AwsClient1
import net.kotlinx.aws1.AwsInstanceTypeUtil
import net.kotlinx.aws1.s3.getObjectText
import net.kotlinx.aws1.s3.putObject
import net.kotlinx.core1.exception.KnownException
import net.kotlinx.core2.concurrent.coroutineExecute
import net.kotlinx.core2.gson.GsonData
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 설정된 로직을 실행해주는 도구
 *
 * */
class BatchStepModule(
    private val aws: AwsClient1,
    private val config: BatchStepConfig,
    block: BatchStepModule.() -> Unit,
) {

    val delegate: MutableMap<String, BatchStepRuntimeConfig> = mutableMapOf()

    private val log = KotlinLogging.logger {}
    private val workDir = File(AwsInstanceTypeUtil.instanceType.root, "InvokeModule").apply { mkdirs() }

    init {
        this.apply(block)
    }

    /** 설정 등록 */
    fun register(block: BatchStepRuntimeConfig.() -> Unit) {
        val data = BatchStepRuntimeConfig().apply(block)
        delegate[data.batchStepRuntime::class.simpleName!!] = data
    }

    data class InvokeResult(
        /** 입력값 */
        val input: String,
        /** 결과값 */
        val result: String,
        /** 걸린시간 */
        val durationMills: Long,
    )

    /** 직접 호출 */
    fun execute(name: String, datas: Any, interval: Duration?): List<InvokeResult> {
        val invokeData = delegate[name] ?: throw IllegalArgumentException("$name not found")

        interval?.let {
            val invokeAbleTime = invokeData.eventTimeChecker.check(interval)
            if (!invokeAbleTime) throw KnownException.ItemRetryException("잠시 후 다시 시도하세요")
        }
        return GsonData.parse(datas).map {
            suspend {
                val start = System.currentTimeMillis()
                val result = invokeData.batchStepRuntime.executeEach(it)
                InvokeResult(it.toString(), result, System.currentTimeMillis() - start)
            }
        }.coroutineExecute().filterNotNull() //일단
    }

    /** S3 이벤트로 호출 */
    suspend fun handleRequest(event: Map<String, Any>) {
        val start = System.currentTimeMillis()
        log.trace { "입력파일 로드.." }
        val key = event[key]?.toString() ?: throw IllegalArgumentException("Key is rquired")
        val fileName = key.substringAfterLast("/")
        val sfnId = key.substringBeforeLast("/").substringAfterLast("/")
        val inputJson = aws.s3.getObjectText(config.workUploadBuket, key)!!
        val inputData = GsonData.parse(inputJson)

        log.trace { "데이터 처리.." }
        val name = inputData[name].str ?: throw IllegalArgumentException("name is rquired -> $inputData")
        val datas = inputData[datas]
        check(!datas.empty) { "datas is rquired -> $inputData" }
        val interval = inputData[intervalSec].str?.let { it.toDouble().seconds }

        val outputData = execute(name, datas, interval)
        log.trace { "데이터 처리 성공 : ${outputData.size}건" }

        log.trace { "결과파일 생성.." }
        val lines = outputData.map {
            listOf(
                sfnId,
                fileName,
                it.input,
                it.result,
                it.durationMills
            )
        }
        val outFile = File(workDir, "$fileName.csv")
        csvWriter().writeAll(lines, outFile)
        aws.s3.putObject(config.workUploadBuket, "${config.workUploadOutputDir}${sfnId}/${outFile.name}", outFile)

        log.trace { "입력파일 삭제.." }
        aws.s3.deleteObject {
            this.bucket = config.workUploadBuket
            this.key = key
        }

    }

    companion object {
        /** AWS가 S3 객체를 넣어줄때 사용하는 키값 (S3경로) */
        const val key = "Key"

        const val name = "name"
        const val datas = "datas"
        const val intervalSec = "intervalSec"
    }


}


