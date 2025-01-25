package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import aws.sdk.kotlin.services.s3.deleteObject
import aws.sdk.kotlin.services.s3.model.NoSuchKey
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.s3.s3
import net.kotlinx.aws.with
import net.kotlinx.core.Kdsl
import net.kotlinx.file.gzip
import net.kotlinx.file.slash
import net.kotlinx.file.slashDir
import net.kotlinx.file.unGzip
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.koin.Koins.koinOrCheck
import net.kotlinx.time.TimeStart
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
class S3LogicHandler {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient by koinLazy()
    private val eventBus: EventBus by koinLazy()

    @Kdsl
    constructor(block: S3LogicHandler.() -> Unit = {}) {
        apply(block)
    }

    /**
     * 버킷은 단일로 고정이다.
     * AWS에서 버킷을 전달 해주는지는 모름.. (해줄거 같은데..) 해주면 그때가서 수정 (수정 간단함)
     * 일단 컨텍스트 용량을 줄이기 위해서 버킷은 여기 등록하는걸로 하자.
     *  */
    lateinit var workBucket: String

    /** 로컬 작업공간 */
    private val workDir = AwsInstanceTypeUtil.INSTANCE_TYPE.root.slashDir(S3LogicHandler::class.simpleName!!)

    /** 직접 호출 */
    suspend fun execute(input: S3LogicInput): S3LogicOutput {
        val s3Logic = koinOrCheck<S3Logic>(input.logicId)
        log.debug { " -> ${s3Logic.id} 실행" }
        return s3Logic.runtime.executeLogic(input)
    }

    /** 람다 핸들러 호출 */
    suspend fun execute(s3InputDataKey: String) {
        val start = TimeStart()
        val inputData = run {
            log.trace { "입력파일(S3) 로드.." }

            //csv로 안하고 그냥 한덩어리로 읽음
            val inputJsonText = aws.s3.with {
                //getObjectText(workBucket, s3InputDataKey)
                val localInput = workDir.slash("input.txt.gz") //이름은 상관없음.
                localInput.delete()
                try {
                    getObjectDownload(workBucket, s3InputDataKey, localInput)
                    localInput.unGzip().readText()
                } catch (e: NoSuchKey) {
                    null
                }
            }
            if (inputJsonText == null) {
                log.warn { "파일없음으로 스킵 $s3InputDataKey" } //리스팅 중에 다른 람다에서 처리가 완료된 경우
                return
            }
            log.trace { "입력파일(S3) 로드 -> $s3InputDataKey ->  ${inputJsonText.length} length" }
            S3LogicInput.parseJson(inputJsonText)
        }

        log.trace { "데이터 처리.." }
        val path = S3LogicPath(s3InputDataKey)
        val s3LogicOutput = try {
            execute(inputData)
        } catch (e: Exception) {
            eventBus.post(S3LogicFailEvent(path, inputData, e))
            return
        }

        val resultJson = GsonData.obj {
            put("sfn_id", path.pathId)
            put("file_name", path.fileName)
            put("total_size", inputData.datas.size)
            put("total_interval", start.interval())  //전체 걸린시간
            put("input", s3LogicOutput.input)
            put("output", s3LogicOutput.result)
        }

        val outFile =  File(workDir, "${path.fileName}.json").run {
            writeText(resultJson.toString())
            gzip()
        }

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
        log.info { "데이터 처리성공. ${path.fileName} ${inputData.datas.size}건 -> $start" }
    }

}


