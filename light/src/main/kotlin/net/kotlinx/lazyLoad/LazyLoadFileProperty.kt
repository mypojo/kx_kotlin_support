package net.kotlinx.lazyLoad

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.aws.ssm.find
import net.kotlinx.core.ProtocolPrefix
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.number.toSiText
import net.kotlinx.okhttp.download
import okhttp3.OkHttpClient
import java.io.File
import kotlin.reflect.KProperty


/**
 * 각종 리소스에서 늦은 로드를 해주는 프로퍼티
 *  */
class LazyLoadFileProperty(

    /** 설정된 파일 */
    private val configFile: File,
    /** 파일에 기록할 내용의 위치 정보 */
    private val path: String,
) {

    /**
     * 실제 위임 파일
     * 디렉토리 입력일 경우 path로 재조정 해줌 ex) S3 파일
     *  */
    private var delegateFile = when {
        configFile.isDirectory -> configFile.slash(path.substringAfterLast("/"))
        else -> configFile
    }

    /** 데이터가 없으면 로드한다. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): File {
        if (delegateFile.exists()) {
            log.trace { " -> File이 이미 존재합니다. 로드 스킵!! $path" }
        } else {
            load()
        }
        return delegateFile
    }

    fun load(): File {
        when {
            path.startsWith(ProtocolPrefix.HTTP) || path.startsWith(ProtocolPrefix.HTTPS) -> {
                log.debug { " -> File을 http에서 로드합니다 $path -> $delegateFile" }
                val http: OkHttpClient = koin()
                http.download(delegateFile) {
                    this.url = path
                }
                log.debug { " -> File을 http에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
            }

            path.startsWith(ProtocolPrefix.S3) -> {
                log.debug { " -> File을 S3에서 로드합니다 $path -> $delegateFile" }
                val s3Data = S3Data.parse(path)
                val aws: AwsClient1 = koin()
                runBlocking {
                    aws.s3.getObjectDownload(s3Data.bucket, s3Data.key, delegateFile)
                }
                log.debug { " -> File을 S3에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
            }

            path.startsWith(ProtocolPrefix.SSM) -> {
                val ssmPath = path.removePrefix(ProtocolPrefix.SSM)
                log.debug { " -> File을 AWS Parameter Store 에서 로드합니다 $ssmPath -> $delegateFile" }
                val aws: AwsClient1 = koin()
                val value = runBlocking {
                    aws.ssm.find(ssmPath) ?: throw java.lang.IllegalArgumentException("SSM 값이 없습니다. $ssmPath")
                }
                delegateFile.writeText(value)
                log.debug { " -> File을 S3에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
            }

            else -> throw IllegalArgumentException("지원하지 않는 형식입니다. $path")
        }
        return delegateFile
    }

    /** 도중에 변경 가능하다!! */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: File) {
        this.delegateFile = value
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}

//==================================================== 간단호출 ======================================================

/** 늦은 초기화  & 중위함수 지원 */
infix fun File.lazyLoad(path: String): LazyLoadFileProperty = LazyLoadFileProperty(this, path)

/** 늦은 초기화  & 중위함수 지원 */
infix fun File.lazyLoad(path: S3Data): LazyLoadFileProperty = LazyLoadFileProperty(this, path.toFullPath())

infix fun File.lazyLoadSsm(path: String): LazyLoadFileProperty = LazyLoadFileProperty(this, "${ProtocolPrefix.SSM}$path")