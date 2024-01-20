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

/**
 * presign 업로드 URL을 리턴해준다.
 * metadata 를 사용해서 DDB를 사용하지 않고도 각종 파라메터 정보를 선입력 가능하다.
 * 참고!!  업로드 크기 체크는 불가능하다. -> 하지만 AWS는 업로드 공짜임으로 람다를 이용해서 삭제 해주면 된다.
 *  */
suspend fun S3Client.presignPutObject(bucket: String, key: String, duration: Duration = 5.minutes, metadata: Map<String, String>? = null): Url {
    val req = PutObjectRequest {
        this.bucket = bucket
        this.key = key
        this.metadata = metadata
    }
    return this.presignPutObject(req, duration).url
}

