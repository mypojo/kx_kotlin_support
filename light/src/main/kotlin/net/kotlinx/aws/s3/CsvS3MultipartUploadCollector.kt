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
import net.kotlinx.number.toSiText
import java.io.ByteArrayOutputStream

/**
 * FlowCollector 기반 CSV S3 멀티파트 업로드 콜렉터
 * lambda 등에서 ->  메모리와 디스크용량이 부족할때 -> 대용량 파일을 업로드할때 사용됨
 * - emit() 으로 전달되는 레코드(List<List<String>>)를 CSV로 버퍼에 기록하고, splitMb 임계에 도달하면 멀티파트 업로드 수행
 * - close() 시 남은 데이터를 업로드하여 마무리 (필요 시 단일 PUT)
 *
 * 주의!! 테스트 필요함
 */
class CsvS3MultipartUploadCollector : FlowCollector<List<List<String>>>, AutoCloseable {

    @Kdsl
    constructor(block: CsvS3MultipartUploadCollector.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정값 ======================================================

    /** S3 클라이언트 */
    lateinit var s3: S3Client

    /** 버킷 */
    lateinit var bucket: String

    /** 키 (경로) */
    lateinit var key: String

    /** 파트 분할 기준(MB). 기본 1GB. 최대 5GB */
    var splitMb: Int = 1024

    /** 메타데이터 (베이스64 인코딩되어 저장됨) */
    var metadata: Map<String, String>? = null

    /** CSV Writer 설정 */
    var csv: CsvWriter = csvWriter()

    /** 헤더 (첫 파트에만 기록) */
    var header: List<String>? = null

    //==================================================== 내부 상태 =====================================================

    private val partSize: Long get() = splitMb * 1024L * 1024L
    private var partNumber: Int = 1
    private var uploadId: String? = null
    private val completedParts: MutableList<CompletedPart> = mutableListOf()

    private var buffer: ByteArrayOutputStream = ByteArrayOutputStream()
    private var rawWriter: CsvFileWriter? = null
    private var writerOpened: Boolean = false

    companion object {
        private val log = KotlinLogging.logger {}
    }

    //==================================================== 내부 로직 =====================================================

    private suspend fun ensureInitiated() {
        if (uploadId == null) {
            val res = s3.createMultipartUpload {
                this.bucket = bucket
                this.key = key
                this.metadata = metadata?.map { it.key to it.value.encodeBase64() }?.toMap()
            }
            uploadId = res.uploadId
            log.info { "  ==> s3 CSV MultipartUpload 시작.. partSize=${partSize.toSiText()}" }
        }
    }

    private fun openWriter(firstPart: Boolean) {
        rawWriter = csv.openAndGetRawWriter(buffer)
        if (firstPart) header?.let { rawWriter!!.writeRow(it) }
        writerOpened = true
    }

    private suspend fun uploadCurrentBufferAndReset() {
        rawWriter?.close()
        val bytes = buffer.toByteArray()
        if (bytes.isEmpty()) {
            buffer = ByteArrayOutputStream()
            rawWriter = csv.openAndGetRawWriter(buffer)
            return
        }
        ensureInitiated()
        val req = UploadPartRequest {
            this.bucket = bucket
            this.key = key
            this.uploadId = uploadId
            this.partNumber = partNumber
            this.body = ByteStream.fromBytes(bytes)
        }
        log.trace { "  --> [${partNumber}] ${bytes.size.toLong().toSiText()} upload... " }
        val res = s3.uploadPart(req)
        completedParts.add(
            CompletedPart {
                partNumber = partNumber
                eTag = res.eTag
            }
        )
        partNumber += 1
        buffer = ByteArrayOutputStream()
        rawWriter = csv.openAndGetRawWriter(buffer)
        // 헤더는 첫 파트에만 기록
    }

    //==================================================== FlowCollector 구현 ============================================

    override suspend fun emit(lines: List<List<String>>) {
        check(splitMb > 0)
        check(splitMb <= 1024 * 5) { "1회당 업로드 크기는 최대 5GB 용량 지원" }

        if (!writerOpened) openWriter(firstPart = true)
        lines.forEach { row ->
            rawWriter!!.writeRow(row)
            if (buffer.size().toLong() >= partSize) {
                // 임계 도달 시 현재 버퍼 업로드
                uploadCurrentBufferAndReset()
            }
        }
    }

    //==================================================== AutoCloseable 구현 ============================================

    override fun close() {
        // 네트워크 호출 필요 → runBlocking 사용
        rawWriter?.close()
        val remaining = buffer.toByteArray()
        runBlocking {
            when {
                uploadId == null && completedParts.isEmpty() -> {
                    if (remaining.isEmpty()) return@runBlocking
                    s3.putObject(bucket, key, ByteStream.fromBytes(remaining), metadata)
                    log.info { "  ==> s3 CSV Upload (single PUT) ${remaining.size.toLong().toSiText()} 완료" }
                }

                else -> {
                    if (remaining.isNotEmpty()) {
                        val req = UploadPartRequest {
                            this.bucket = bucket
                            this.key = key
                            this.uploadId = uploadId
                            this.partNumber = partNumber
                            this.body = ByteStream.fromBytes(remaining)
                        }
                        log.trace { "  --> [${partNumber}] ${remaining.size.toLong().toSiText()} upload (last)... " }
                        val res = s3.uploadPart(req)
                        completedParts.add(
                            CompletedPart {
                                partNumber = partNumber
                                eTag = res.eTag
                            }
                        )
                    }

                    s3.completeMultipartUpload {
                        this.bucket = bucket
                        this.key = key
                        this.uploadId = uploadId
                        this.multipartUpload = CompletedMultipartUpload {
                            this.parts = completedParts
                        }
                    }
                    log.info { "  ==> s3 CSV MultipartUpload 완료 (${completedParts.size} parts)" }
                }
            }
        }
    }
}
