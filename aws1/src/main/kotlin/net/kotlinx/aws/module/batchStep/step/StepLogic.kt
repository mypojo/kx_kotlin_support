package net.kotlinx.aws.module.batchStep.step

import aws.sdk.kotlin.services.s3.deleteObject
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.s3.getObjectText
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.with
import net.kotlinx.core.csv.CsvUtil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.core.time.TimeStart
import net.kotlinx.core.time.toTimeString
import java.io.File

/**
 * 크롤링, API 호출 등등 모든 실제 로직이 여기서 실행됨
 * 개발 편의상 모든 입력은 S3 으로 돝일 (돈 얼마 안함. 천번 호출해야 $0.0045 정도 / S3 삭제로 진행완료 판단)
 * 출력은 S3로 통일 (이게 편함)
 *  */
class StepLogic(
    private val bsConfig: BatchStepConfig,
) : StepHandler {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient1 = bsConfig.aws

    /** 로컬 작업공간 */
    private val workDir = File(AwsInstanceTypeUtil.instanceType.root, StepLogic::class.simpleName).apply { mkdirs() }


    /** 직접 호출 */
    suspend fun execute(input: StepLogicInput): List<StepLogicOutput> {
        val runtime = bsConfig.delegate[input.logicName] ?: throw IllegalArgumentException("${input.logicName} not found")
        log.debug { " -> ${runtime::class.simpleName} 실행" }
        return runtime.executeLogic(input)
    }

    /** 람다 핸들러 호출 */
    override suspend fun handleRequest(event: Map<String, Any>): Any {

        val start = TimeStart()

        val input = run {
            log.trace { "입력파일(S3) 로드.." }

            val inputKey = event[KEY]?.toString() ?: throw IllegalArgumentException("아직 지원하지 않는 형식입니다.  -> $event")

            //csv로 안하고 그냥 한덩어리로 읽음
            val inputJson = aws.s3.with { getObjectText(bsConfig.workUploadBuket, inputKey) }
            if (inputJson == null) {
                log.warn { "파일없음으로 스킵 $inputKey" } //리스팅 중에 다른 람다에서 처리가 완료된 경우
                return LambdaUtil.FAIL
            }
            log.trace { "입력파일(S3) 로드 -> $inputKey ->  ${inputJson.length} length" }
            val inputData = GsonData.parse(inputJson)
            inputData.fromJson<StepLogicInput>().apply {
                this.inputKey = inputKey
                this.fileName = inputKey.substringAfterLast("/")
                this.sfnId = inputKey.substringBeforeLast("/").substringAfterLast("/")
            }
        }

        log.trace { "데이터 처리.." }
        val outputData = try {
            execute(input)
        } catch (e: Exception) {
            log.warn { "###### 데이터 처리 실패!! -> $start / ${input.fileName} / ${e.toSimpleString()}" }
            return LambdaUtil.FAIL //리트라이 하지 않음으로 예외를 던지지 않고 실패 리턴함 (로그 지저분해짐)
        }
        log.trace { "데이터 처리 성공 : ${outputData.size}건" }
        val interval = start.interval()
        val lines = outputData.map {
            listOf(
                input.sfnId,
                input.fileName,
                input.datas.size, //전체 처리 수
                interval, //전체 걸린시간
                it.input,
                it.result,
                it.durationMills //개별 시간을 기록함
            )
        }

        val outFile = File(workDir, "${input.fileName}.csv.gz")
        CsvUtil.writeAllGzip(outFile, lines)
        val outputKey = "${bsConfig.workUploadOutputDir}${input.sfnId}/${outFile.name}"
        log.trace { "결과파일 생성.. $outputKey" }
        aws.s3.with { putObject(bsConfig.workUploadBuket, outputKey, outFile) }

        log.trace { "입력파일 삭제.." }
        aws.s3.with {
            deleteObject {
                this.bucket = bsConfig.workUploadBuket
                this.key = input.inputKey
            }
        }
        val average = outputData.map { it.durationMills }.average().toLong().toTimeString()
        log.warn { "데이터 처리성공. ${input.fileName} ${lines.size}건 -> $start / 개별처리평균 $average" }
        return obj {
            "fileName" to input.fileName
            "size" to lines.size
            "totalDuration" to start.toString()
            "eachDuration" to average.toString()
        }
    }

    companion object {
        /**
         * AWS가 S3 객체를 넣어줄때 사용하는 기본 키값을 동일하게 사용
         * 주의! 두문자가 대문자임
         * */
        const val KEY = "Key"

    }


}


