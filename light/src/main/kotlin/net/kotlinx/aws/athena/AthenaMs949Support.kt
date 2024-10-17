import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toInputStream
import mu.KotlinLogging
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.athena.AthenaMs949
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.s3.s3
import net.kotlinx.csv.CsvReadWriteTool
import net.kotlinx.string.CharSets


/**
 * 쿼리 실행 -> MS949로 다운로드 -> S3 업로드 -> 프리사인URL 획득
 *  */
suspend fun AthenaModule.download(block: AthenaMs949.() -> Unit): AthenaMs949 {

    val log = KotlinLogging.logger {}

    val ct = AthenaMs949(block)
    ct.athenaModule = this

    log.trace { " -> 쿼리 실행.." }
    val execute = execute(ct.query)
    ct.queryResultPath = S3Data.parse(execute.outputLocation)

    log.trace { " -> 결과 CSV를 MS949로 변환.. ${ct.queryResultPath}" }
    aws.s3.getObject(
        GetObjectRequest {
            this.bucket = ct.queryResultPath.bucket
            this.key = ct.queryResultPath.key
        }
    ) {
        CsvReadWriteTool {
            readerInputStream = it.body?.toInputStream()!!
            writerFile = ct.queryResultFile
            writerCharset(CharSets.MS949)
            writerGzip = ct.gzip
            processor = ct.processor
        }
    }

    ct.uploadInfo?.let {
        log.debug { " -> 파일 업로드 됩니다.. $it" }
        val data = it.first
        aws.s3.putObject(data.bucket, data.key, ct.queryResultFile, it.second)
    }

    return ct

}
