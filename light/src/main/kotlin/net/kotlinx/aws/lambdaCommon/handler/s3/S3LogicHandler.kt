package net.kotlinx.aws.lambdaCommon.handler.s3

import aws.sdk.kotlin.services.s3.deleteObject
import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.aws.s3.getObjectText
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.with
import net.kotlinx.core.csv.CsvUtil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.core.time.TimeStart
import net.kotlinx.core.time.toTimeString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * S3 경로 입력에 반응하는 핸들러. (커스텀 입력 or SFN 자동실행)
 * 크롤링, API 호출 등등 모든 실제 로직이 여기서 실행됨
 * 개발 편의상 모든 입력은 S3 으로 통일했음 (돈 얼마 안함. 천번 호출해야 $0.0045 정도 / S3 삭제로 진행완료 판단)
 *
 * #1 S3 파일을 읽고
 * #2 지정된 로직을 실행하고
 * #3 결과를 S3에 업로드
 *  */
class S3LogicHandler(
    /**
     * 버킷은 단일로 고정이다.
     * AWS에서 버킷을 전달 해주는지는 모름.. (해줄거 같은데..) 해주면 그때가서 수정 (수정 간단함)
     * 일단 컨텍스트 용량을 줄이기 위해서 버킷은 여기 등록하는걸로 하자.
     *  */
    private val workBucket: String,
    block: S3LogicHandler.() -> Unit
) : LambdaLogicHandler, KoinComponent {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient1 by inject()

    /** 커스텀 로직들 */
    val logicMap: MutableMap<String, S3Logic> = mutableMapOf()

    /** 설정 등록 */
    fun register(block: S3Logic.() -> Unit = {}) {
        val s3Logic = S3Logic().apply(block)
        logicMap[s3Logic.id] = s3Logic
    }

    init {
        block(this)
    }

    /** 로컬 작업공간 */
    private val workDir = File(AwsInstanceTypeUtil.INSTANCE_TYPE.root, S3LogicHandler::class.simpleName).apply { mkdirs() }

    /** 직접 호출 */
    suspend fun execute(input: S3LogicInput): List<S3LogicOutput> {
        val stepLogic = logicMap[input.logicName] ?: throw IllegalArgumentException("${input.logicName} not found")
        log.debug { " -> ${stepLogic.id} 실행" }
        return stepLogic.runtime.executeLogic(input)
    }

    /** 람다 핸들러 호출 */
    override suspend fun invoke(event: GsonData, context: Context?): Any? {

        val s3InputDataKey = event[KEY].str ?: return null //S3 형식이 아니라면 스킵. 여기 full path S3 경로가 전달된다.

        val start = TimeStart()
        val input = run {
            log.trace { "입력파일(S3) 로드.." }

            //csv로 안하고 그냥 한덩어리로 읽음
            val inputJsonText = aws.s3.with { getObjectText(workBucket, s3InputDataKey) }
            if (inputJsonText == null) {
                log.warn { "파일없음으로 스킵 $s3InputDataKey" } //리스팅 중에 다른 람다에서 처리가 완료된 경우
                return LambdaUtil.FAIL
            }
            log.trace { "입력파일(S3) 로드 -> $s3InputDataKey ->  ${inputJsonText.length} length" }
            S3LogicInput.parseJson(inputJsonText)
        }

        log.trace { "데이터 처리.." }
        val path = S3LogicPath(s3InputDataKey)
        val outputData = try {
            execute(input)
        } catch (e: Exception) {
            log.warn { "###### 데이터 처리 실패!! -> $start / ${path.fileName} / ${e.toSimpleString()}" }
            return LambdaUtil.FAIL //리트라이 하지 않음으로 예외를 던지지 않고 실패 리턴함 (로그 지저분해짐)
        }

        //일부러 한줄이 아니라 여러줄 형태로 기록한다. (한줄에 너무 많은 데이터가 있으면 사람이 읽기 힘들 수 있기 때문에)
        log.trace { "데이터 처리 성공 : ${outputData.size}건" }
        val interval = start.interval()
        val lines = outputData.map {
            listOf(
                path.pathId,
                path.fileName,
                input.datas.size, //전체 처리 수
                interval, //전체 걸린시간
                it.input,
                it.result,
                it.durationMills //개별 시간을 기록함
            )
        }

        val outFile = File(workDir, "${path.fileName}.csv.gz")
        CsvUtil.writeAllGzip(outFile, lines)
        val outputKey = "${path.outputDir}${path.pathId}/${outFile.name}"
        log.trace { "결과파일 생성.. $outputKey" }
        aws.s3.with { putObject(workBucket, outputKey, outFile) }

        log.trace { "입력파일 삭제.." }
        aws.s3.with {
            deleteObject {
                this.bucket = workBucket
                this.key = path.s3InputDataKey
            }
        }
        val average = outputData.map { it.durationMills }.average().toLong().toTimeString()
        log.info { "데이터 처리성공. ${path.fileName} ${lines.size}건 -> $start / 개별처리평균 $average" }
        return obj {
            "fileName" to path.fileName
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


