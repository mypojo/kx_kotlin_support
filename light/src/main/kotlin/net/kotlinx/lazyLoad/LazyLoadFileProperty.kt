package net.kotlinx.lazyLoad

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.aws.s3.listFiles
import net.kotlinx.aws.s3.s3
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
class LazyLoadFileProperty(val inputFile: File) {

    //==================================================== 설정 ======================================================

    /** 파일에 기록할 내용의 위치 정보 */
    lateinit var info: String

    /** 프로파일 정보 */
    var profile: String? = null

    //==================================================== 로직 ======================================================

    /**
     * 실제 위임 파일
     *  */
    private var delegateFile = inputFile

    /** 데이터가 없으면 로드한다. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): File {
        if (delegateFile.exists() && delegateFile.isFile) {
            log.trace { " -> File이 이미 존재합니다. 로드 스킵!! $info" }
        } else {
            load()
        }
        return delegateFile
    }

    /** 외부에서 강제 로드도 가능하다 */
    fun load() {
        when {
            info.startsWith(ProtocolPrefix.HTTP) || info.startsWith(ProtocolPrefix.HTTPS) -> {
                log.debug { " -> File을 http에서 로드합니다 $info -> $delegateFile" }
                val http: OkHttpClient = koin()
                http.download(delegateFile) {
                    this.url = info
                }
                log.debug { " -> File을 http에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
            }

            info.startsWith(ProtocolPrefix.S3) -> {
                log.debug { " -> File을 S3에서 로드합니다 $info -> $delegateFile" }
                val aws: AwsClient1 = koin(profile)
                val input = S3Data.parse(info)
                runBlocking {
                    if (input.isDirectory) {
                        val s3Files = aws.s3.listFiles(input.bucket, input.key)
                        s3Files.forEach { s3File ->
                            val eachFile = delegateFile.slash(s3File.fileName)
                            if (eachFile.exists()) return@forEach

                            aws.s3.getObjectDownload(s3File.bucket, s3File.key, eachFile)
                        }
                        log.debug { " -> File을 S3에서 ${s3Files.size}건 로드 완료 -> ${delegateFile.listFiles().sumOf { it.length() }.toSiText()} : ${delegateFile.absolutePath}" }
                    } else {
                        aws.s3.getObjectDownload(input.bucket, input.key, delegateFile)
                        log.debug { " -> File을 S3에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
                    }
                }
            }

            info.startsWith(ProtocolPrefix.SSM) -> {
                val ssmPath = info.removePrefix(ProtocolPrefix.SSM)
                log.debug { " -> File을 AWS Parameter Store 에서 로드합니다 $ssmPath -> $delegateFile" }
                val aws: AwsClient1 = koin(profile)
                val value = runBlocking {
                    aws.ssm.find(ssmPath) ?: throw java.lang.IllegalArgumentException("SSM 값이 없습니다. $ssmPath")
                }
                delegateFile.writeText(value)
                log.debug { " -> File을 S3에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
            }

            else -> throw IllegalArgumentException("지원하지 않는 형식입니다. $info")
        }
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

/** 프로파일 설정 등 상세 설정을 다 할때 */
infix fun File.lazyLoad(block: LazyLoadFileProperty.() -> Unit): LazyLoadFileProperty = LazyLoadFileProperty(this).apply(block)

/** 늦은 초기화  & 중위함수 지원 */
infix fun File.lazyLoad(path: String): LazyLoadFileProperty = lazyLoad {
    this.info = path
}

/** 늦은 초기화  & 중위함수 지원 */
infix fun File.lazyLoad(path: S3Data): LazyLoadFileProperty = this.lazyLoad(path.toFullPath())

/** 늦은 초기화 by SSM */
infix fun File.lazyLoadSsm(path: String): LazyLoadFileProperty = this.lazyLoad("${ProtocolPrefix.SSM}$path")