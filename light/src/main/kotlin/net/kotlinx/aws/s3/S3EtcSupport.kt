package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.*
import aws.sdk.kotlin.services.s3.model.ObjectIdentifier
import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import aws.smithy.kotlin.runtime.client.config.RequestHttpChecksumConfig
import aws.smithy.kotlin.runtime.client.config.ResponseHttpChecksumConfig
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.concurrent.coroutineExecute

/** 삭제, 리스트 조회 등 이게 디폴트인듯  */
const val LIMIT_PER_REQ = 1000

val AwsClient.s3: S3Client
    get() = getOrCreateClient {
        S3Client {
            awsConfig.build(this)
            requestChecksumCalculation = RequestHttpChecksumConfig.WHEN_SUPPORTED
            responseChecksumValidation = ResponseHttpChecksumConfig.WHEN_SUPPORTED
        }.regist(awsConfig)
    }

//==================================================== move ======================================================
/**
 * 대상 디렉토리를 변경한다
 * ex) DDB 변환후 지정된 장소로 이동
 * 실제 move는 존재하지 않고, 카피 후 삭제 해야함.
 * 기본 설정으로 메타데이터는 동일하게 복제됨
 *  */
suspend fun S3Client.moveDir(fromDir: S3Data, toDir: S3Data) {
    val fromDatas = this.listObjects(fromDir.bucket, fromDir.key)
    for (fromData in fromDatas) {
        this.copyObject {
            this.copySource = fromData.toPath()
            this.bucket = toDir.bucket
            this.key = toDir.key + fromData.fileName
        }
    }
    this.deleteAll(fromDatas)
}

/** 파일 이동 단건 버전 */
suspend fun S3Client.moveFile(fromFile: S3Data, toFile: S3Data) {
    copyObject {
        copySource = fromFile.toPath()
        bucket = toFile.bucket
        key = toFile.key
    }
    deleteObject {
        bucket = fromFile.bucket
        key = fromFile.key
        versionId = fromFile.versionId
    }
}

//==================================================== list ======================================================

/**
 * 디렉토리를 가져온다.
 * 경고!! AWS는 S3 리스팅에 desc를 지원하지 않는다. 따라서 디렉토링을 잘 할것!
 * @param prefix  디렉토리 표시인 /로 끝나면 디렉토리로 인싱한다  ex) main/data/
 * */
suspend fun S3Client.listDirs(bucket: String, prefix: String): List<S3Data> = this.listObjectsV2 {
    this.bucket = bucket
    delimiter = "/"
    this.prefix = prefix
}.commonPrefixes?.map { S3Data(bucket, it.prefix!!) } ?: emptyList() //파일이 있더라도 디렉토리가 없다면 빈값이 올 수 있음

/**
 * 디렉토리 전체 가져옴
 * */
suspend fun S3Client.listAllDirs(bucket: String, prefix: String): List<S3Data> = this.listObjectsV2Paginated {
    this.bucket = bucket
    delimiter = "/"
    this.prefix = prefix
}.map {
    it.commonPrefixes?.map { S3Data(bucket, it.prefix!!) } ?: emptyList()
}.toList().flatten()

/** 파일(객체)들을 가져온다. */
suspend fun S3Client.listObjects(bucket: String, prefix: String): List<S3Data> = this.listObjectsV2 {
    this.bucket = bucket
    this.prefix = prefix
}.contents?.map { S3Data(bucket, it.key!!).apply { this.obj = it } } ?: emptyList()

/** 파일(객체)들을 가져온다. */
suspend fun S3Client.listAllObjects(bucket: String, prefix: String): List<S3Data> = this.listObjectsV2Paginated {
    this.bucket = bucket
    this.prefix = prefix
}.map { r ->
    r.contents?.map { S3Data(bucket, it.key!!).apply { this.obj = it } } ?: emptyList()
}.toList().flatten()

/** 간단 메소드 */
suspend fun S3Client.listObjects(s3data: S3Data): List<S3Data> = this.listObjects(s3data.bucket, s3data.key)

//==================================================== 삭제 ======================================================

/** 자주 사용하는거라 등록함 */
suspend fun S3Client.deleteDir(bucket: String, prefix: String): Int = this.listObjects(bucket, prefix).also { deleteAll(it) }.size

/**  버킷 단위로 벌크 삭제한다. 대부분 페이징해서 호출할테니 별도의 사이즈 제한은 없음  */
fun S3Client.deleteAll(datas: Collection<S3Data>) {
    val groupByBucket = datas.groupBy { it.bucket } //버킷별로 호출
    groupByBucket.entries.map { (bucket, list) ->
        suspend {
            list.chunked(LIMIT_PER_REQ).mapIndexed { _, each ->
                //log.debug { "버킷 [$bucket] : [${index + 1}/${list.size}] -> ${each.size} 건 삭제" }
                suspend {
                    this.deleteObjects {
                        this.bucket = bucket
                        this.delete {
                            this.objects = each.map {
                                ObjectIdentifier {
                                    this.key = it.key
                                    this.versionId = it.versionId
                                }
                            }
                        }
                    }
                }
            }.coroutineExecute(8)
        }
    }.coroutineExecute() //AWS 동시호출에 어차피 제한이 있으니 일단 버킷 단위로만 동시 처리한다.
}