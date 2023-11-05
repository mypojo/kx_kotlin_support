package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.sdk.kotlin.services.s3.presigners.presignPutObject
import aws.smithy.kotlin.runtime.net.Url
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** presign 다운로드 URL을 리턴해준다. */
suspend fun S3Client.presignGetObject(bucket: String, key: String, duration: Duration = 5.minutes, contentType: String? = null): Url {
    val req = GetObjectRequest {
        this.bucket = bucket
        this.key = key
        this.responseContentType = contentType
    }
    return this.presignGetObject(req, duration).url
}

/** presign 업로드 URL을 리턴해준다. */
suspend fun S3Client.presignGetObject(bucket: String, key: String, duration: Duration = 5.minutes): Url {
    val req = PutObjectRequest {
        this.bucket = bucket
        this.key = key
    }
    return this.presignPutObject(req, duration).url
}

