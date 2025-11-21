package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.text.encoding.encodeBase64
import java.io.File


/**
 * 간단 업로드
 * @param key 업로드 디렉토리 path.  /로 시작하지 않음!!
 *  */
suspend inline fun S3Client.putObject(bucket: String, key: String, byteStream: ByteStream, metadata: Map<String, String>? = null) {
    this.putObject {
        this.bucket = bucket
        this.key = key
        this.body = byteStream
        this.metadata = metadata?.map { it.key to it.value.encodeBase64() }?.toMap()
    }
}

/**
 * 자동 간단 파일 업로드
 * 1기가(GB) 이하이면 단일 putObject, 그 외(> 1GB)는 멀티파트 업로드로 처리
 *  */
suspend inline fun S3Client.putObject(bucket: String, key: String, file: File, metadata: Map<String, String>? = null) {
    when {
        // 1GB 초과 시 멀티파트 사용
        file.length() > 1024L * 1024 * 1024 -> putObjectMultipart(bucket, key, file, metadata = metadata)
        else -> putObject(bucket, key, file.asByteStream(), metadata)
    }
}

/** 업로드 단축 */
suspend inline fun S3Client.putObject(path: S3Data, file: File, metadata: Map<String, String>? = null) = putObject(path.bucket, path.key, file, metadata)

/** 바이트 업로드 */
suspend inline fun S3Client.putObject(bucket: String, key: String, byteArray: ByteArray) = putObject(bucket, key, ByteStream.fromBytes(byteArray))
