package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.model.HeadObjectResponse
import net.kotlinx.aws.toKoreaDateTime
import java.time.LocalDate

//==================================================== 아래는 필드 확인용 ======================================================

/** 파일 크기 */
val HeadObjectResponse.size: Long? get() = contentLength

/** 컨텐츠 타입 */
val HeadObjectResponse.contentTypeName: String? get() = contentType

/** 마지막 수정일 (한국시간 기준 날짜만) */
val HeadObjectResponse.lastModifiedDate: LocalDate? get() = lastModified?.toKoreaDateTime()?.toLocalDate()

/**
 * [HeadObjectResponse] 의 데이터 중 의미 있는 정보만 추출하여 맵으로 리턴
 * null 인 값은 제외한다.
 */
fun HeadObjectResponse.toSummaryMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    acceptRanges?.let { map["acceptRanges"] = it }
    archiveStatus?.let { map["archiveStatus"] = it.toString() }
    bucketKeyEnabled?.let { map["bucketKeyEnabled"] = it.toString() }
    cacheControl?.let { map["cacheControl"] = it }
    checksumCrc32?.let { map["checksumCrc32"] = it }
    checksumCrc32C?.let { map["checksumCrc32C"] = it }
    checksumCrc64Nvme?.let { map["checksumCrc64Nvme"] = it }
    checksumSha1?.let { map["checksumSha1"] = it }
    checksumSha256?.let { map["checksumSha256"] = it }
    checksumType?.let { map["checksumType"] = it.toString() }
    contentDisposition?.let { map["contentDisposition"] = it }
    contentEncoding?.let { map["contentEncoding"] = it }
    contentLanguage?.let { map["contentLanguage"] = it }
    contentLength?.let { map["contentLength"] = it.toString() }
    contentRange?.let { map["contentRange"] = it }
    contentType?.let { map["contentType"] = it }
    deleteMarker?.let { map["deleteMarker"] = it.toString() }
    eTag?.let { map["eTag"] = it }
    expiration?.let { map["expiration"] = it }
    expires?.let { map["expires"] = it.toString() }
    lastModified?.let { map["lastModified"] = it.toString() }
    metadata?.takeIf { it.isNotEmpty() }?.let {
        map["metadata"] = it.toString()
    }
    missingMeta?.let { map["missingMeta"] = it.toString() }
    objectLockLegalHoldStatus?.let { map["objectLockLegalHoldStatus"] = it.toString() }
    objectLockMode?.let { map["objectLockMode"] = it.toString() }
    objectLockRetainUntilDate?.let { map["objectLockRetainUntilDate"] = it.toString() }
    partsCount?.let { map["partsCount"] = it.toString() }
    replicationStatus?.let { map["replicationStatus"] = it.toString() }
    requestCharged?.let { map["requestCharged"] = it.toString() }
    restore?.let { map["restore"] = it }
    serverSideEncryption?.let { map["serverSideEncryption"] = it.toString() }
    sseCustomerAlgorithm?.let { map["sseCustomerAlgorithm"] = it }
    sseCustomerKeyMd5?.let { map["sseCustomerKeyMd5"] = it }
    ssekmsKeyId?.let { map["ssekmsKeyId"] = it }
    storageClass?.let { map["storageClass"] = it.toString() }
    tagCount?.let { map["tagCount"] = it.toString() }
    versionId?.let { map["versionId"] = it }
    websiteRedirectLocation?.let { map["websiteRedirectLocation"] = it }
    return map
}

/**
 * 의미 있는 정보만 문자열로 변환
 */
fun HeadObjectResponse.toSummaryString(): String {
    return toSummaryMap().entries.joinToString(", ", "HeadObjectResponse(", ")") { "${it.key}=${it.value}" }
}
