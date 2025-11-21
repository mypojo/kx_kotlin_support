package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.abortMultipartUpload
import aws.sdk.kotlin.services.s3.completeMultipartUpload
import aws.sdk.kotlin.services.s3.createMultipartUpload
import aws.sdk.kotlin.services.s3.model.CompletedMultipartUpload
import aws.sdk.kotlin.services.s3.model.CompletedPart
import aws.sdk.kotlin.services.s3.model.UploadPartRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.text.encoding.encodeBase64
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.kotlinx.core.VibeCoding
import net.kotlinx.number.toSiText
import java.io.File
import java.io.RandomAccessFile

/**
 * 멀티파트 업로드 -> 병렬 전송 버전 (네트워크/메모리가 충분할 때 사용)
 * - 각 파트를 병렬로 업로드하여 대용량 업로드 속도를 개선함
 * - 예외 발생 시 업로드를 즉시 중단하고 S3에 Abort 요청 수행 후 예외를 다시 던짐
 *
 * @param key 업로드 디렉토리 path.  /로 시작하지 않음!!
 * @param splitMb 분할 처리할 용량(MB). S3 제약: 최소 5MB, 최대 5GB
 * @param parallelism 동시 업로드 개수. 너무 크게 설정하면 메모리 사용량이 급증함 (메모리 사용량 ≒ splitMb * parallelism)
 */
@VibeCoding
suspend inline fun S3Client.putObjectMultipartParallel(bucket: String, key: String, file: File, splitMb: Int = 20, parallelism: Int = 4, metadata: Map<String, String>? = null) {

    val log = KotlinLogging.logger {}

    check(splitMb > 0)
    check(splitMb <= 1024 * 5) { "1회당 업로드 크기는 최대 5GB 용량 지원" }
    check(splitMb >= 5) { "1회당 업로드 크기는 최소 5MB 용량 지원" }
    check(parallelism > 0) { "parallelism 은 1 이상이어야 합니다" }

    val fileSize = file.length()

    val initiate = this.createMultipartUpload {
        this.bucket = bucket
        this.key = key
        this.metadata = metadata?.map { it.key to it.value.encodeBase64() }?.toMap()
    }

    val partSize = splitMb * 1024L * 1024L
    val numParts = ((fileSize + partSize - 1) / partSize).toInt()

    log.debug { "  ==> s3 MultipartUpload(Parallel x${parallelism}) [${file}] ${fileSize.toSiText()} -> ${numParts}분할(${partSize.toSiText()}) 업로드 시작.." }

    try {
        // 병렬 업로드 실행
        val parts: List<CompletedPart> = coroutineScope {
            (0 until numParts).map { index ->
                async(Dispatchers.IO) {
                    val startPos = index * partSize
                    val currentPartSize = if (index + 1 == numParts) (fileSize - startPos) else partSize

                    // 각 파트별로 필요한 만큼만 읽어서 메모리에 적재
                    val buffer = withContext(Dispatchers.IO) {
                        RandomAccessFile(file, "r").use { raf ->
                            raf.seek(startPos)
                            ByteArray(currentPartSize.toInt()).also { raf.readFully(it) }
                        }
                    }

                    log.trace { "  --> [${index + 1}/${numParts}] ${currentPartSize.toSiText()} upload... " }

                    val uploadPartResult = this@putObjectMultipartParallel.uploadPart(
                        UploadPartRequest {
                            this.bucket = bucket
                            this.key = key
                            this.uploadId = initiate.uploadId
                            this.partNumber = index + 1
                            this.body = ByteStream.fromBytes(buffer)
                        }
                    )

                    CompletedPart {
                        partNumber = index + 1
                        eTag = uploadPartResult.eTag
                    }
                }
            }.chunked(parallelism).flatMap { batch ->
                // 병렬 제한을 위해 배치 단위로 await
                batch.awaitAll()
            }
        }

        // 파트 병합 (partNumber 오름차순 보장)
        val sortedParts = parts.sortedBy { it.partNumber }

        this.completeMultipartUpload {
            this.bucket = bucket
            this.key = key
            this.uploadId = initiate.uploadId
            this.multipartUpload = CompletedMultipartUpload {
                this.parts = sortedParts
            }
        }
    } catch (e: Exception) {
        // 실패 시 Abort 시도 후 재던짐
        runCatching {
            this.abortMultipartUpload {
                this.bucket = bucket
                this.key = key
                this.uploadId = initiate.uploadId
            }
        }.onFailure { abortErr ->
            log.warn { "AbortMultipartUpload 실패: ${abortErr.message}" }
        }
        throw e
    }
}
