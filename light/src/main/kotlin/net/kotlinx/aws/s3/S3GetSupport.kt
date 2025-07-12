package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.getObjectAttributes
import aws.sdk.kotlin.services.s3.headObject
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.NoSuchKey
import aws.sdk.kotlin.services.s3.model.NotFound
import aws.sdk.kotlin.services.s3.model.ObjectAttributes
import aws.smithy.kotlin.runtime.content.decodeToString
import aws.smithy.kotlin.runtime.content.toByteArray
import aws.smithy.kotlin.runtime.content.toInputStream
import aws.smithy.kotlin.runtime.content.writeToFile
import aws.smithy.kotlin.runtime.text.encoding.decodeBase64
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.takeWhile
import net.kotlinx.csv.CsvCollector
import net.kotlinx.csv.CsvUtil
import net.kotlinx.csv.toFlow
import net.kotlinx.io.input.toInputResource
import net.kotlinx.io.output.toOutputResource
import java.io.File
import java.nio.charset.Charset


//==================================================== 다운로드 ======================================================

/**
 * 간단 다운로드. 스트리밍 처리시 다운받아서 하세여 (inputStream 제공이 없는거 같음.)
 * 메타데이터 체크를 통과해야 다운로드 한다
 * */
suspend fun S3Client.getObjectDownload(bucket: String, key: String, file: File, shouldDownload: (Map<String, String>?) -> Boolean = { true }) {
    this.getObject(
        GetObjectRequest {
            this.bucket = bucket
            this.key = key
        }
    ) {
        val doDownload = shouldDownload(it.metadata)
        if (doDownload) {
            it.body?.writeToFile(file)
        }
    }
}


/**
 * CSV ms949 형태로 변경하면서 다운로드함. -> 다운로드 받은다음 다시 변환하는 이중작업을 피하기 위함임
 * ex) athena 결과파일을 사용자가 볼 수 있는 형태로 다운로드
 * 샘플 코드임! 응용해서 사용할것
 * 자주 사용해서 일단 넣었고, 거의 응용이 필요하지 않아서 별도의 옵션화는 하지 않음
 * @see CsvSplitCollector  파일 청크단위로 분리해서 다운로드할때 사용
 *  */
suspend fun S3Client.getObjectCsvMs949Download(bucket: String, key: String, file: File, zip: Boolean = false) {
    this.getObjectCsvFlow(bucket, key, csvReader()) { flow ->
        flow.chunked(1000).collect(CsvCollector {
            outputResource = file.toOutputResource(zip)
            writer = CsvUtil.ms949Writer()  //ms949로 다운로드
        })
    }
}

//==================================================== 속성만 읽기 ======================================================

/** 파일 크기만 읽고싶을때 */
suspend fun S3Client.getObjectSize(data: S3Data) {
    this.getObjectAttributes {
        this.bucket = data.bucket
        this.key = data.key
        this.objectAttributes = listOf(
            ObjectAttributes.ObjectSize
        )
    }.objectSize
}

//==================================================== 간단 읽기 ======================================================
/**
 * 간단 읽기 (소용량)
 * AWS kotlin에서 아직 스트림 읽기는 안되는거 같음. ByteStream 을 일반 스트림으로 어케 바꾸지?? -> 일단 java SDK2를 사용할것
 * 간단 쓰기는 없음 (스트리핑 put은 안됨) -> 먼저 파일로 쓴 다음 업로드 할것!!
 * @param charset  지정하지 않으면 기본디코딩
 *
 * => CSV 등을 스트림으로 읽고싶다면? -> 안하는게 좋음.  안전성을 위해서 다운받은 후 읽어라.
 *  */
suspend fun S3Client.getObjectLines(bucket: String, key: String, charset: Charset? = null): List<List<String>>? {
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
    } catch (_: NoSuchKey) {
        null
    }
}

/**
 * 스트리밍 CSV 읽기
 * ex) 대용량 파일의 앞에 100개만 읽기
 * 안정성 떨어짐 주의!
 * @see getObjectCsvMs949Download  -> 이런식으로 사용가능
 * @see takeWhile -> 조건부 읽기 가능
 * 짧게 읽고 끊으면 , 읽음 만큼만 과금됨
 * */
suspend fun S3Client.getObjectCsvFlow(bucket: String, key: String, reader: CsvReader = csvReader(), action: suspend (Flow<List<String>>) -> Unit) = this.getObject(
    GetObjectRequest {
        this.bucket = bucket
        this.key = key
    }
) {
    val flow = it.body?.toInputStream()!!.toInputResource().toFlow(reader)
    action(flow)
}


/** 단축 */
suspend fun S3Client.getObjectText(s3data: S3Data): String? = getObjectText(s3data.bucket, s3data.key)

/**
 * 없으면 null을 리턴한다
 * 데이터를 가져오는 시간은 양이 적을경우 2자리 밀리초 걸림
 * 동시 100건 코루틴 처리도 문제없음(타임아웃 정도만 조심)
 *  */
suspend fun S3Client.getObjectText(bucket: String, key: String): String? {
    return try {
        this.getObject(
            GetObjectRequest {
                this.bucket = bucket
                this.key = key
            }
        ) {
            it.body!!.decodeToString()
        }
    } catch (_: NoSuchKey) {
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
suspend fun S3Client.getObjectMetadata(bucket: String, key: String): Map<String, String>? {
    return try {
        val resp = this.headObject {
            this.bucket = bucket
            this.key = key
        }
        resp.metadata?.map { it.key to it.value.decodeBase64() }?.toMap() ?: emptyMap()
    } catch (_: NoSuchKey) {
        null
    } catch (_: NotFound) {
        null
    }
}