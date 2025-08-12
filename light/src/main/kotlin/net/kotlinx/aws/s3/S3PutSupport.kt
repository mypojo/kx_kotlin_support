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

/** 파일 업로드 */
suspend inline fun S3Client.putObject(bucket: String, key: String, file: File, metadata: Map<String, String>? = null) {
    when {
        file.length() > 1024 * 1024 * 10 -> putObjectMultipart(bucket, key, file, metadata = metadata)
        else -> putObject(bucket, key, file.asByteStream(), metadata)
    }
}

/** 업로드 단축 */
suspend inline fun S3Client.putObject(path: S3Data, file: File, metadata: Map<String, String>? = null) = putObject(path.bucket, path.key, file, metadata)

/** 바이트 업로드 */
suspend inline fun S3Client.putObject(bucket: String, key: String, byteArray: ByteArray) = putObject(bucket, key, ByteStream.fromBytes(byteArray))
