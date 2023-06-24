package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.exportTableToPointInTime
import aws.sdk.kotlin.services.dynamodb.listExports
import aws.sdk.kotlin.services.dynamodb.model.ExportFormat
import aws.sdk.kotlin.services.dynamodb.model.ExportStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.core.time.measureTimeString
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * https://aws.amazon.com/ko/blogs/korea/new-export-amazon-dynamodb-table-data-to-data-lake-amazon-s3/
 * 2023 서울기준 GB당 0.1083 USD
 * */
class DynamoDbExporter(private val aws: AwsClient1, block: DynamoDbExporter.() -> Unit = {}) {

    val log = KotlinLogging.logger {}

    val client = aws.dynamo

    /** 테이블 명 */
    lateinit var tableName: String

    /** 버킷 */
    lateinit var s3Bucket: String

    /** 접두어 */
    lateinit var s3Prefix: String

    /** ION도 써보고 싶네 */
    var exportFormat = ExportFormat.DynamodbJson

    /** 최대 체크 타임아웃 시간 */
    var checkTimeout: Long = TimeUnit.MINUTES.toMillis(10)

    /** 최초 시작 딜레이. 스타트에 5분 정도 걸리는듯  */
    var startDelay = 5.minutes //최소 5분 정도는 걸리는듯

    /** 체크 딜레이 */
    var waitDelay = 20.seconds

    /** 얼마나 체크 반복할지 */
    var repeatCnt = 100


    init {
        block(this)
    }

    suspend fun exportAndWait() {
        measureTimeString {
            val tableArn = "arn:aws:dynamodb:${aws.awsConfig.region}:${aws.awsConfig.awsId}:table/${tableName}"
            val resp = client.exportTableToPointInTime {
                this.tableArn = tableArn
                this.s3Bucket = this@DynamoDbExporter.s3Bucket
                this.s3Prefix = this@DynamoDbExporter.s3Prefix
                this.exportFormat = this@DynamoDbExporter.exportFormat
            }
            val exportArn = resp.exportDescription!!.exportArn

            withTimeout(checkTimeout) {

                delay(startDelay) //최소 5분 정도는 걸리는듯

                repeat(repeatCnt) {
                    val target = client.listExports { this.tableArn }.exportSummaries!!.first { it.exportArn == exportArn }
                    when (target.exportStatus!!) {
                        ExportStatus.InProgress -> log.debug { " -> 진행중.." }
                        ExportStatus.Failed -> throw IllegalStateException("Failed!!")
                        ExportStatus.Completed -> return@repeat
                        else -> throw IllegalStateException("${target.exportStatus} is not required")
                    }
                    delay(waitDelay)
                }
            }
        }.also {
            log.info { "작업종료. 걸린시간 $it" }
        }

    }

}