package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.completeMultipartUpload
import aws.sdk.kotlin.services.s3.createMultipartUpload
import aws.sdk.kotlin.services.s3.model.CompletedMultipartUpload
import aws.sdk.kotlin.services.s3.model.CompletedPart
import aws.sdk.kotlin.services.s3.model.UploadPartRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.text.encoding.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.kotlinx.number.toSiText
import java.io.File
import java.io.FileInputStream

/**
 * 멀티파트 업로드
 * @param key 업로드 디렉토리 path.  /로 시작하지 않음!!
 * @param splitMb 분할처리할 용량 -> 대량파일 성능 올리고싶으면 늘려서 지정. 보통 람다에서 작동함으로 최소화 지정함.
 *
 * 주의!! 다운로드는 스트리밍이 되지만 (전체 크기가 정해져있음으로) 업로드는 스트리밍이 까다로움 (전체 크기를 미리 지정해야 함으로)
 *  */
suspend inline fun S3Client.putObjectMultipart(bucket: String, key: String, file: File, splitMb: Int = 5, metadata: Map<String, String>? = null) {

    check(splitMb > 0)
    check(splitMb <= 1024 * 5) { "1회당 업로드 크기는 최대 5GB 용량 지원" }
    check(splitMb >= 5) { "1회당 업로드 크기는 최소 5MB 용량 지원" }

    val log = KotlinLogging.logger {}

    val fileInputStream = withContext(Dispatchers.IO) { FileInputStream(file) }  //스래드 블록때문에 감싸줘야함(inspector경고)
    val fileSize = file.length()

    val initiateMPUResult = this.createMultipartUpload {
        this.bucket = bucket
        this.key = key
        this.metadata = metadata?.map { it.key to it.value.encodeBase64() }?.toMap()
    }

    val partSize = splitMb * 1024L * 1024L
    val numParts = ((fileSize + partSize - 1) / partSize).toInt()

    log.debug { "  ==> s3 MultipartUpload [${file}] ${fileSize.toSiText()} -> ${numParts}분할(${partSize.toSiText()}) 업로드 시작.." }

    val partETags = (0 until numParts).map { i ->
        val startPos = i * partSize
        val currentPartSize = if (i + 1 == numParts) (fileSize - startPos) else partSize
        val buffer = ByteArray(currentPartSize.toInt())
        fileInputStream.read(buffer)

        val uploadPartRequest = UploadPartRequest {
            this.bucket = bucket
            this.key = key
            this.uploadId = initiateMPUResult.uploadId
            this.partNumber = i + 1
            this.body = ByteStream.fromBytes(buffer)
        }
        log.trace { "  --> [${i + 1}/${numParts}] ${currentPartSize.toSiText()} upload... " }
        val uploadPartResult = this.uploadPart(uploadPartRequest)
        CompletedPart {
            partNumber = i + 1
            eTag = uploadPartResult.eTag
        }
    }

    /** 업로드된 조각들 병합 */
    this.completeMultipartUpload {
        this.bucket = bucket
        this.key = key
        this.uploadId = initiateMPUResult.uploadId
        this.multipartUpload = CompletedMultipartUpload {
            this.parts = partETags
        }
    }
}