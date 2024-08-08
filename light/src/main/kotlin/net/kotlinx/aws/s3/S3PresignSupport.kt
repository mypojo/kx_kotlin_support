package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.sdk.kotlin.services.s3.presigners.presignPutObject
import net.kotlinx.core.Kdsl
import net.kotlinx.string.encodeUrl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class S3PresignGet {

    @Kdsl
    constructor(block: S3PresignGet.() -> Unit = {}) {
        apply(block)
    }

    /** 버킷  */
    lateinit var bucket: String

    /** 키 */
    lateinit var key: String

    /** 컨텐츠 타입 없으면 디폴트 */
    var contentType: String? = null

    /** 다운로드 명 없으면 디폴트 (S3 객체 이름 그대로) */
    var downloadName: String? = null

    /**
     * 다운로드 가능한 기간.
     * web 의 경우 적게 줘도 되지만
     * 메신저 등의 링크에는 길제 줘야 한다.
     *  */
    var duration: Duration = 5.minutes

}

/**
 * presign 다운로드 URL을 리턴해준다.
 * 일단 toString 으로..
 *  */
suspend fun S3Client.presignGetObjectUrl(block: S3PresignGet.() -> Unit = {}): String {
    val param = S3PresignGet(block)
    val req = GetObjectRequest {
        bucket = param.bucket
        key = param.key
        responseContentType = param.contentType
        param.downloadName?.let { responseContentDisposition = "attachment; filename=${it.encodeUrl()}" }
    }
    return presignGetObject(req, param.duration).url.toString()
}

/**
 * presign 다운로드 URL을 리턴 단축
 *  */
suspend fun S3Client.presignGetObject(bucket: String, key: String, duration: Duration = 5.minutes): String = presignGetObjectUrl {
    this.bucket = bucket
    this.key = key
    this.duration = duration
}

/**
 * presign 업로드 URL을 리턴해준다.  1개 파일당 1개가 매핑되니 주의!
 * metadata 를 사용해서 DDB를 사용하지 않고도 각종 파라메터 정보를 선입력 가능하다.
 * 참고!!  업로드 크기 체크는 불가능하다. -> 하지만 AWS는 업로드 공짜임으로 람다를 이용해서 삭제 해주면 된다.
 *  */
suspend fun S3Client.presignPutObject(bucket: String, key: String, duration: Duration = 5.minutes, metadata: Map<String, String>? = null): String {
    val req = PutObjectRequest {
        this.bucket = bucket
        this.key = key
        this.metadata = metadata
    }
    val presignPutObject = this.presignPutObject(req, duration)
    return presignPutObject.url.toString()
}

