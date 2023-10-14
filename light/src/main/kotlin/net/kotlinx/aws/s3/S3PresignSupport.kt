package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presign
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/** presign 다운로드 URL을 리턴해준다. */
suspend inline fun S3Client.getObjectPresign(
    bucket: String,
    key: String,
    duration: Duration = 1.toDuration(DurationUnit.HOURS),
    contentType: String? = null,
) = GetObjectRequest {
    this.bucket = bucket
    this.key = key
    this.responseContentType = contentType
}.presign(this.config, duration = duration).url

/** presign 업로드 URL을 리턴해준다. */
suspend inline fun S3Client.putObjectPresign(
    bucket: String,
    key: String,
    duration: Duration = 1.toDuration(DurationUnit.HOURS),
) = PutObjectRequest {
    this.bucket = bucket
    this.key = key
}.presign(this.config, duration = duration).url

