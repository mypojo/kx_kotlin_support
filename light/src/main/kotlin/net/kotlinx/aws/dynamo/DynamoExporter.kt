package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.describeExport
import aws.sdk.kotlin.services.dynamodb.exportTableToPointInTime
import aws.sdk.kotlin.services.dynamodb.model.ExportFormat
import aws.sdk.kotlin.services.dynamodb.model.ExportStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.time.measureTimeString
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * https://aws.amazon.com/ko/blogs/korea/new-export-amazon-dynamodb-table-data-to-data-lake-amazon-s3/
 * 2023 서울기준 GB당 0.1083 USD -> 매우 저렴함
 * */
class DynamoExporter(private val aws: AwsClient, block: DynamoExporter.() -> Unit = {}) {

    private val log = KotlinLogging.logger {}

    /** 테이블 명 */
    lateinit var tableName: String

    /** 버킷 */
    lateinit var s3Bucket: String

    /**
     * 결과 저장소 접두어.  /로 끝나야함
     * 어차피 옮길거니 임시 디렉토리를 지정하면 됨
     *  */
    lateinit var s3Prefix: String

    /** ION이 제일 좋다. 디폴트로 사용할것. */
    var exportFormat: ExportFormat = ExportFormat.Ion

    /** 최대 체크 타임아웃 시간 */
    var checkTimeout: Long = TimeUnit.MINUTES.toMillis(10)

    /** 최초 시작 딜레이. 스타트에 5분 정도 걸리는듯  */
    var startDelay = 5.minutes //최소 5분 정도는 걸리는듯

    /** 체크 딜레이 */
    var waitDelay = 10.seconds

    /** 얼마나 체크 반복할지 */
    var repeatCnt = 100

    init {
        //설정과 실행을 일단 분리함
        block(this)
    }

    /** exportArn  */
    lateinit var exportArn: String

    /** 이 위치에 실제 결과 파일이 생성됨 */
    val s3ResultPath: String
        get() = "${s3Prefix}AWSDynamoDB/${exportArn.substringAfterLast("/")}/data/"


    /**
     * 보통 x 분 걸림
     * */
    suspend fun exportAndWait() {
        measureTimeString {
            export()
            wait()
        }.also {
            log.info { "작업종료. 걸린시간 $it" }
        }
    }

    /** 위치 지정하면, 내부 구조는 변경이 불가능한듯. */
    suspend fun export() {
        val tableArn = "arn:aws:dynamodb:${aws.awsConfig.region}:${aws.awsConfig.awsId}:table/${tableName}"
        val resp = aws.dynamo.exportTableToPointInTime {
            this.tableArn = tableArn
            this.s3Bucket = this@DynamoExporter.s3Bucket
            this.s3Prefix = this@DynamoExporter.s3Prefix
            this.exportFormat = this@DynamoExporter.exportFormat
        }
        exportArn = resp.exportDescription!!.exportArn!!
        log.info { "expor 요청 성공 : exportArn : $exportArn" }
    }

    suspend fun wait() {
        withTimeout(checkTimeout) {

            log.debug { "== export wait for first $startDelay ==" }
            delay(startDelay)

            for (i in 0..repeatCnt) {
                val target = aws.dynamo.describeExport { this.exportArn = this@DynamoExporter.exportArn }.exportDescription!!
                //val target = client.listExports { this.tableArn }.exportSummaries!!.first { it.exportArn == exportArn }
                when (target.exportStatus!!) {
                    ExportStatus.InProgress -> log.debug { " -> 진행중.." }
                    ExportStatus.Failed -> throw IllegalStateException("실패!! DDB 콘솔의 Exports to S3 에서 로그를 확인해주세요")
                    ExportStatus.Completed -> break
                    else -> throw IllegalStateException("${target.exportStatus} is not required")
                }
                log.debug { " -> expor [${i + 1}/${repeatCnt}] 대기중 ..  ${target.exportStatus}" }
                delay(waitDelay)
            }
            log.info { "== export Completed ==" }

        }
    }

}