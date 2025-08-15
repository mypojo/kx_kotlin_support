package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.completeMultipartUpload
import aws.sdk.kotlin.services.s3.createMultipartUpload
import aws.sdk.kotlin.services.s3.model.CompletedMultipartUpload
import aws.sdk.kotlin.services.s3.model.CompletedPart
import aws.sdk.kotlin.services.s3.model.UploadPartRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.text.encoding.encodeBase64
import com.github.doyaaaaaken.kotlincsv.client.CsvFileWriter
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.core.VibeCoding
import net.kotlinx.io.output.toOutputResource
import java.io.File
import java.nio.file.Files

/**
 * FlowCollector 기반 CSV S3 업로드 콜렉터
 * lambda 처럼 메모리와 디스크 모두 부족환 환경에서 대용량을 처리하기 위한 도구임
 *
 * - emit() 으로 전달되는 레코드(List<List<String>>)를 로컬 파일에 CSV로 기록
 * - 파일 크기가 임계값(기본 5MB)을 넘으면 멀티파트 업로드 수행
 * - 임계값 미만이면 단일 PUT으로 업로드
 * - close() 시 남은 데이터를 업로드하여 마무리
 */
@VibeCoding
class CsvS3MultipartUploadCollector : FlowCollector<List<List<String>>>, AutoCloseable {

    @Kdsl
    constructor(block: CsvS3MultipartUploadCollector.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정값 ======================================================

    /** S3 클라이언트 */
    lateinit var s3: S3Client

    /** S3 데이터 (버킷과 키) */
    lateinit var s3Data: S3Data

    /** 멀티파트 업로드 임계값 (MB). 기본 5MB (S3 최소값) */
    var multipartThresholdMb: Int = 5

    /** 메타데이터 (베이스64 인코딩되어 저장됨) */
    var metadata: Map<String, String>? = null

    /** CSV Writer 설정 */
    var csvWriter: CsvWriter = csvWriter()

    /** 헤더 (첫 파트에만 기록) */
    var header: List<String>? = null

    //==================================================== 내부 상태 =====================================================

    private var partNumber: Int = 1
    private var uploadId: String? = null
    private val completedParts: MutableList<CompletedPart> = mutableListOf()

    private var currentFile: File? = null
    private var csvRawWriter: CsvFileWriter? = null
    private val multipartThresholdBytes: Long get() = multipartThresholdMb * 1024L * 1024L
    private var headerWritten: Boolean = false

    companion object {
        private val log = KotlinLogging.logger {}
    }

    //==================================================== 내부 로직 =====================================================

    private suspend fun ensureInitiated() {
        if (uploadId == null) {
            val res = s3.createMultipartUpload {
                this.bucket = s3Data.bucket
                this.key = s3Data.key
                this.metadata = metadata?.map { it.key to it.value.encodeBase64() }?.toMap()
            }
            uploadId = res.uploadId
            log.info { "  ==> s3 CSV MultipartUpload 시작.. threshold=${multipartThresholdMb}MB" }
        }
    }

    private fun createNewFile() {
        currentFile = Files.createTempFile("csv_part_", ".csv").toFile()
        csvRawWriter = csvWriter.openAndGetRawWriter(currentFile!!.toOutputResource().outputStream)

        // 첫 번째 파트에만 헤더 추가
        if (!headerWritten) {
            header?.let {
                csvRawWriter!!.writeRow(it)
            }
            headerWritten = true
        }
    }


    //==================================================== FlowCollector 구현 ============================================

    override suspend fun emit(lines: List<List<String>>) {
        check(multipartThresholdMb >= 5) { "multipartThresholdMb는 5MB 이상이어야 합니다" }

        if (csvRawWriter == null) createNewFile()

        lines.forEach { row ->
            csvRawWriter!!.writeRow(row)
        }
    }

    //==================================================== AutoCloseable 구현 ============================================

    override fun close() {
        runBlocking {
            csvRawWriter?.close()
            csvRawWriter = null

            val file = currentFile
            if (file != null && file.exists() && file.length() > 0) {
                val fileSize = file.length()

                if (fileSize < multipartThresholdBytes) {
                    // 임계값 미만이면 단일 PUT
                    val bytes = file.readBytes()
                    s3.putObject(s3Data.bucket, s3Data.key, ByteStream.fromBytes(bytes), metadata)
                    log.info { "  ==> s3 CSV Upload (single PUT) ${bytes.size} bytes 완료" }
                } else {
                    // 임계값 이상이면 멀티파트 업로드로 분할
                    uploadFileAsMultipart(file)
                }

                file.delete()
            }

            // 임시 파일 정리
            currentFile?.let { if (it.exists()) it.delete() }
        }
    }

    private suspend fun uploadFileAsMultipart(file: File) {
        ensureInitiated()

        val currentUploadId = uploadId ?: throw IllegalStateException("uploadId가 null입니다")

        val bytes = file.readBytes()
        val partSize = multipartThresholdBytes.toInt()
        var offset = 0
        var currentPartNumber = 1

        while (offset < bytes.size) {
            val endOffset = minOf(offset + partSize, bytes.size)
            val partBytes = bytes.sliceArray(offset until endOffset)

            val req = UploadPartRequest {
                this.bucket = s3Data.bucket
                this.key = s3Data.key
                this.uploadId = currentUploadId
                this.partNumber = currentPartNumber
                this.body = ByteStream.fromBytes(partBytes)
            }

            log.debug { "  --> [${currentPartNumber}] ${partBytes.size} bytes upload..." }
            val res = s3.uploadPart(req)
            completedParts.add(
                CompletedPart {
                    partNumber = currentPartNumber
                    eTag = res.eTag
                }
            )

            offset = endOffset
            currentPartNumber++
        }

        // 멀티파트 업로드 완료
        s3.completeMultipartUpload {
            this.bucket = s3Data.bucket
            this.key = s3Data.key
            this.uploadId = currentUploadId
            this.multipartUpload = CompletedMultipartUpload {
                this.parts = completedParts
            }
        }
        log.info { "  ==> s3 CSV MultipartUpload 완료 (${completedParts.size} parts)" }
    }
}
