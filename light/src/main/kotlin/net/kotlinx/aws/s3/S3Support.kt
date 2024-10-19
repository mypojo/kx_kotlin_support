package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.*
import aws.sdk.kotlin.services.s3.model.*
import aws.smithy.kotlin.runtime.content.*
import aws.smithy.kotlin.runtime.text.encoding.decodeBase64
import aws.smithy.kotlin.runtime.text.encoding.encodeBase64
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.number.toSiText
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

/** 삭제, 리스트 조회 등 이게 디폴트인듯  */
const val LIMIT_PER_REQ = 1000

val AwsClient.s3: S3Client
    get() = getOrCreateClient { S3Client { awsConfig.build(this) }.regist(awsConfig) }

//==================================================== 기본 읽기/쓰기 ======================================================

/**
 * 간단 다운로드. 스트리밍 처리시 다운받아서 하세여 (inputStream 제공이 없는거 같음.)
 * 메타데이터 체크를 통과해야 다운로드 한다
 * */
suspend inline fun S3Client.getObjectDownload(bucket: String, key: String, file: File, crossinline block: (Map<String, String>?) -> Boolean = { true }) = this.getObject(
    GetObjectRequest {
        this.bucket = bucket
        this.key = key
    }
) {
    val doDownload = block(it.metadata)
    if (doDownload) {
        it.body?.writeToFile(file)
    }
}


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

/** 바이트 업로드 */
suspend inline fun S3Client.putObject(bucket: String, key: String, byteArray: ByteArray) = putObject(bucket, key, ByteStream.fromBytes(byteArray))

/**
 * 멀티파트 업로드
 * @param key 업로드 디렉토리 path.  /로 시작하지 않음!!
 * @param splitMb 분할처리할 용량
 *  */
suspend inline fun S3Client.putObjectMultipart(bucket: String, key: String, file: File, splitMb: Int = 1024, metadata: Map<String, String>? = null) {

    check(splitMb > 0)
    check(splitMb <= 1024 * 5) { "1회당 업로드 크기는 최대 5GB 용량 지원" }

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

    log.info { "s3 MultipartUpload (${file}) ${fileSize.toSiText()} / ${partSize.toSiText()} -> ${numParts}분할 업로드 시작.." }

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


//==================================================== 속성만 읽기 ======================================================

/** 파일 크기만 읽고싶을때 */
suspend inline fun S3Client.objectSize(data: S3Data) {
    this.getObjectAttributes {
        this.bucket = data.bucket
        this.key = data.key
        this.objectAttributes = listOf(
            ObjectAttributes.ObjectSize
        )
    }.objectSize
}

//==================================================== move ======================================================
/**
 * 대상 디렉토리를 변경한다
 * ex) DDB 변환후 지정된 장소로 이동
 * 실제 move는 존재하지 않고, 카피 후 삭제 해야함.
 * 기본 설정으로 메타데이터는 동일하게 복제됨
 *  */
suspend fun S3Client.moveDir(fromDir: S3Data, toDir: S3Data) {
    val fromDatas = this.listFiles(fromDir.bucket, fromDir.key)
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
 * @param prefix  디렉토리 표시인 /로 끝나면 디렉토리로 인싱한다  ex) main/data/
 * */
suspend inline fun S3Client.listDirs(bucket: String, prefix: String): List<S3Data> = this.listObjectsV2 {
    this.bucket = bucket
    delimiter = "/"
    this.prefix = prefix
}.commonPrefixes?.map { S3Data(bucket, it.prefix!!) } ?: emptyList() //파일이 있더라도 디렉토리가 없다면 빈값이 올 수 있음

/**
 * 파일(객체)들을 가져온다.
 * 최대 1천개 가져오니 주의!!
 * 페이징은 별도로 구현할것
 *  */
suspend inline fun S3Client.listFiles(bucket: String, prefix: String): List<S3Data> = this.listObjectsV2 {
    this.bucket = bucket
    this.prefix = prefix
}.contents?.map { S3Data(bucket, it.key!!) } ?: emptyList()

//==================================================== 삭제 ======================================================

/** 자주 사용하는거라 등록함 */
suspend inline fun S3Client.deleteDir(bucket: String, prefix: String): Int = this.listFiles(bucket, prefix).also { deleteAll(it) }.size

/**  버킷 단위로 벌크 삭제한다. 대부분 페이징해서 호출할테니 별도의 사이즈 제한은 없음  */
suspend inline fun S3Client.deleteAll(datas: Collection<S3Data>) {
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

//==================================================== 간단 읽기 ======================================================
/**
 * 간단 읽기 (소용량)
 * AWS kotlin에서 아직 스트림 읽기는 안되는거 같음. ByteStream 을 일반 스트림으로 어케 바꾸지?? -> 일단 java SDK2를 사용할것
 * 간단 쓰기는 없음 (스트리핑 put은 안됨) -> 먼저 파일로 쓴 다음 업로드 할것!!
 * @param charset  지정하지 않으면 기본디코딩
 *  */
@Deprecated("lazyLoad 쓰세요")
suspend inline fun S3Client.getObjectLines(bucket: String, key: String, charset: Charset? = null): List<List<String>>? {
    return try {
        this.getObject(
            GetObjectRequest {
                this.bucket = bucket
                this.key = key
            }
        ) {
            when (charset) {
                null -> csvReader().readAll(it.body!!.decodeToString())
                else -> {
                    val text = String(it.body!!.toByteArray(), charset) //원하는 캐릭터셋으로 인코딩
                    csvReader().readAll(text)
                }
            }
        }
    } catch (e: NoSuchKey) {
        null
    }
}

/**
 * 없으면 null을 리턴한다
 * 데이터를 가져오는 시간은 양이 적을경우 2자리 밀리초 걸림
 * 동시 100건 코루틴 처리도 문제없음(타임아웃 정도만 조심)
 *  */
suspend inline fun S3Client.getObjectText(bucket: String, key: String): String? {
    return try {
        this.getObject(
            GetObjectRequest {
                this.bucket = bucket
                this.key = key
            }
        ) {
            it.body!!.decodeToString()
        }
    } catch (e: NoSuchKey) {
        null
    }
}

/**
 * 본문 안읽고 User defined 메타데이터만 읽음
 * User defined 메타데이터는 x-amz-meta-aa 이런식으로 입력되지만 , 실제 가져오면 정상 출력됨
 * 사용자 정의 메타데이터는 크기가 2KB로 제한
 * @return 파일이 없으면(NotFound) null을 리턴  => 파일이 있는지 체크 여부에서 사용됨
 *
 * getObject 를 사용해서 body를 읽지 않는거하고 동일한 로직인듯
 * */
suspend inline fun S3Client.getObjectMetadata(bucket: String, key: String): Map<String, String>? {
    return try {
        val resp = this.headObject {
            this.bucket = bucket
            this.key = key
        }
        resp.metadata?.map { it.key to it.value.decodeBase64() }?.toMap() ?: emptyMap()
    } catch (e: NoSuchKey) {
        null
    } catch (e: NotFound) {
        null
    }
}