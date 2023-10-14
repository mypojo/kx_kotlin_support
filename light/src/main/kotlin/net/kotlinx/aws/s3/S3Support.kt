package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.*
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.NoSuchKey
import aws.sdk.kotlin.services.s3.model.ObjectAttributes
import aws.sdk.kotlin.services.s3.model.ObjectIdentifier
import aws.smithy.kotlin.runtime.content.*
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import mu.KotlinLogging
import net.kotlinx.core.concurrent.coroutineExecute
import java.io.File
import java.nio.charset.Charset

/** 삭제, 리스트 조회 등 이게 디폴트인듯  */
const val LIMIT_PER_REQ = 1000

//==================================================== 기본 읽기/쓰기 ======================================================

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
 *  */
suspend inline fun S3Client.moveDir(fromDir: S3Data, toDir: S3Data) {
    val log = KotlinLogging.logger {}
    for (i in 0..100) {
        val fromDatas = this.listFiles(fromDir.bucket, fromDir.key)
        log.info { "moveDir [$i/100] -> ${fromDatas.size}건 이동 : $fromDir -> $toDir" }
        for (fromData in fromDatas) {
            this.copyObject {
                this.copySource = fromData.toPath()
                this.bucket = toDir.bucket
                this.key = toDir.key + fromData.fileName
            }
        }
        this.deleteAll(fromDatas)
        if (fromDatas.size < LIMIT_PER_REQ) break
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

///** 페이징 조회 */
//fun S3Client.getObjectListPaging(s3data: AwsS3Data, pageCnt: Int = 1, pageSize: Int = 100, header: Int = 1): List<Array<String>> {
//    val inputStream = this.getObject(s3data)
//    val inputStreamResource = InputStreamResource(inputStream)
//    val lineToSkip = (pageCnt - 1) * pageSize + header
//    val maxItemCount = pageCnt * pageSize + header
//    return CsvItemReader.of<Array<String>>(inputStreamResource).utf8().linesToSkip(lineToSkip).maxItemCount(maxItemCount).open().readAndClose()
//}

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
