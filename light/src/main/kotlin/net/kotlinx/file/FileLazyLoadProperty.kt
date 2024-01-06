package net.kotlinx.file

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.core.file.slash
import net.kotlinx.core.number.toSiText
import net.kotlinx.okhttp.download
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.reflect.KProperty


/**
 * S3를 사용해서 로드
 * 테스트 필요!!
 *  */
class FileLazyLoadProperty(private val configFile: File, private val path: String) : KoinComponent {

    private var delegateFile = when {
        configFile.isDirectory -> configFile.slash(path.substringAfterLast("/"))
        else -> configFile
    }

    private val aws: AwsClient1 by inject()
    private val http: OkHttpClient by inject()

    /** 데이터가 없으면 로드한다. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): File {
        if (delegateFile.exists()) {
            log.trace { " -> File이 이미 존재합니다. 로드 스킵!! $path" }
        } else {

            when {
                path.startsWith("http://") || path.startsWith("https://") -> {
                    log.debug { " -> File을 http에서 로드합니다 $path -> $delegateFile" }
                    http.download(delegateFile) {
                        this.url = path
                    }
                    log.debug { " -> File을 http에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
                }

                path.startsWith("s3://") -> {
                    log.debug { " -> File을 S3에서 로드합니다 $path -> $delegateFile" }
                    runBlocking {
                        val s3Data = S3Data.parse(path)
                        aws.s3.getObjectDownload(s3Data.bucket, s3Data.key, delegateFile)
                    }
                    log.debug { " -> File을 S3에서 로드 완료 -> ${delegateFile.length().toSiText()} : ${delegateFile.absolutePath}" }
                }

                else -> throw IllegalArgumentException("지원하지 않는 형식입니다. $path")
            }

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

/** 늦은 초기화 */
fun File.lazyLoad(path: String): FileLazyLoadProperty = FileLazyLoadProperty(this, path)