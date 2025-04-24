package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.deleteObject
import aws.smithy.kotlin.runtime.http.request.HttpRequest
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.core.Kdsl
import net.kotlinx.csv.CsvUtil
import net.kotlinx.csv.toFlow
import net.kotlinx.file.slash
import net.kotlinx.io.input.toInputResource
import net.kotlinx.koin.Koins.koin
import net.kotlinx.reflect.name
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * S3 파일을 관리하는 간단한 API 도구
 * CSV 파일을 보관할 목적으로 제작됨
 * */
class S3DirApi {

    @Kdsl
    constructor(block: S3DirApi.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정들 ======================================================

    /** 버킷 */
    lateinit var bucket: String

    /** 키 */
    lateinit var dirPath: String

    /** 프리사인 기간 */
    var presignDuration: Duration = 5.minutes

    /** 프로파일 */
    var profile: String? = null

    /** 기본적으로 UTF-8 읽기 */
    var csvReader: CsvReader = CsvUtil.ms949Reader()

    //==================================================== 내부사용 ======================================================

    private val client by lazy { koin<AwsClient>(profile) }

    //==================================================== 함수들 ======================================================

    /**
     * S3 객체 리스팅
     * 전체를 다 가져옴 주의!
     *  */
    suspend fun list(): List<S3Data> = client.s3.listAllObjects(bucket, dirPath)

    /** 삭제 */
    suspend fun delete(key: String) {
        client.s3.deleteObject {
            this.bucket = this@S3DirApi.bucket
            this.key = key
        }
    }

    /** 다운로드(프리사인 링크) */
    suspend fun downloadLink(key: String): String = client.s3.presignGetObject(bucket, key, presignDuration)

    /** 업로드(프리사인 링크) */
    suspend fun uploadLink(): HttpRequest = client.s3.presignPutObject(bucket, dirPath, presignDuration)

    /**
     * 전체 리스트 데이터를 Flow로 리턴함
     * 안정성때문에 일단 다운받은 후 스트림 처리함
     *  */
    suspend fun readAllDirCsvLines(): List<Flow<List<String>>> {
        val dir = AwsInstanceTypeUtil.INSTANCE_TYPE.tmpDir(this::class.name())
        list().map {
            suspend {
                val file = dir.slash(it.fileName)
                client.s3.getObjectDownload(it.bucket, it.key, file)
            }
        }.coroutineExecute()
        log.debug { " => [${dir.absolutePath}] : S3 download complete. ${dir.listFiles().size} files" }
        return dir.listFiles().toList().map { it.toInputResource().toFlow(csvReader) }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}