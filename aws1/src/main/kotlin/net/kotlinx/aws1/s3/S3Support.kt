package net.kotlinx.aws1.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.deleteObjects
import aws.sdk.kotlin.services.s3.listObjectsV2
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.Object
import aws.sdk.kotlin.services.s3.model.ObjectIdentifier
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.decodeToString
import aws.smithy.kotlin.runtime.content.writeToFile
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.kotlinx.core2.concurrent.coroutineExecute
import java.io.File

/** 삭제, 리스트 조회 등 이게 디폴트인듯  */
const val LIMIT_PER_REQ = 1000

/** 간단 다운로드. 스트리밍 처리시 다운받아서 하세여 (inputStream 제공이 없는거 같음.) */
suspend inline fun S3Client.getObjectDownload(bucket: String, key: String, file: File) = this.getObject(
    GetObjectRequest {
        this.bucket = bucket
        this.key = key
    }
) {
    it.body?.writeToFile(file)
}

/**
 * 간단 업로드
 * @param key 업로드 디렉토리 path.  /로 시작하지 않음!!
 *  */
suspend inline fun S3Client.putObject(bucket: String, key: String, byteStream: ByteStream) {
    this.putObject {
        this.bucket = bucket
        this.key = key
        this.body = byteStream
    }
}

/** 파일 업로드 */
suspend inline fun S3Client.putObject(bucket: String, key: String, file: File) = putObject(bucket, key, file.asByteStream())

/** 바이트 업로드 */
suspend inline fun S3Client.putObject(bucket: String, key: String, byteArray: ByteArray) = putObject(bucket, key, ByteStream.fromBytes(byteArray))

//==================================================== list ======================================================

/**
 * 디렉토리를 가져온다.
 * @param prefix  디렉토리 표시인 /로 끝나면 디렉토리로 인싱한다  ex) main/data/
 * */
suspend inline fun S3Client.listDirs(bucket: String, prefix: String): List<String> = this.listObjectsV2 {
    this.bucket = bucket
    delimiter = "/"
    this.prefix = prefix
}.commonPrefixes?.map { it.prefix!! } ?: emptyList() //파일이 있더라도 디렉토리가 없다면 빈값이 올 수 있음

/** 파일(객체)들을 가져온다. */
suspend inline fun S3Client.listFiles(bucket: String, prefix: String): List<Object> = this.listObjectsV2 {
    this.bucket = bucket
    this.prefix = prefix
}.contents ?: emptyList()

///** 페이징 조회 */
//fun S3Client.getObjectListPaging(s3data: AwsS3Data, pageCnt: Int = 1, pageSize: Int = 100, header: Int = 1): List<Array<String>> {
//    val inputStream = this.getObject(s3data)
//    val inputStreamResource = InputStreamResource(inputStream)
//    val lineToSkip = (pageCnt - 1) * pageSize + header
//    val maxItemCount = pageCnt * pageSize + header
//    return CsvItemReader.of<Array<String>>(inputStreamResource).utf8().linesToSkip(lineToSkip).maxItemCount(maxItemCount).open().readAndClose()
//}

//==================================================== 삭제 ======================================================

/**  버킷 단위로 벌크 삭제한다. 대부분 페이징해서 호출할테니 별도의 사이즈 제한은 없음  */
suspend inline fun S3Client.deleteObjects(datas: Collection<S3Data>) {
    val groupByBucket = datas.groupBy { it.bucket } //버킷별로 호출
    groupByBucket.entries.map { (bucket, list) ->
        suspend {
            list.chunked(LIMIT_PER_REQ).forEachIndexed { index, each ->
                //log.debug { "버킷 [$bucket] : [${index + 1}/${list.size}] -> ${each.size} 건 삭제" }
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
        }
    }.coroutineExecute() //AWS 동시호출에 어차피 제한이 있으니 일단 버킷 단위로만 동시 처리한다.
}

//==================================================== CSV 확장 ======================================================
/**
 * 간단 읽기 (소용량)
 * AWS kotlin에서 아직 스트림 읽기는 안되는거 같음. ByteStream 을 일반 스트림으로 어케 바꾸지?? -> 일단 java SDK2를 사용할것
 * 간단 쓰기는 없음 (스트리핑 put은 안됨) -> 먼저 파일로 쓴 다음 업로드 할것!!
 *  */
suspend inline fun S3Client.getObjectLines(bucket: String, key: String): List<List<String>> = this.getObject(
    GetObjectRequest {
        this.bucket = bucket
        this.key = key
    }
) {
    csvReader().readAll(it.body!!.decodeToString())
}

