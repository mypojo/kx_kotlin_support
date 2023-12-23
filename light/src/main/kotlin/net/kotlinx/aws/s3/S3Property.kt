package net.kotlinx.aws.s3

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import kotlin.reflect.KProperty


/**
 * S3를 사용해서 로드
 * 테스트 필요!!
 *  */
class S3Property(private val path: S3Data) : KoinComponent {

    lateinit var value: File

    operator fun getValue(thisRef: File?, property: KProperty<*>): File {

        if (value.exists()) {
            log.debug { " -> File이 이미 존재합니다. 로드 스킵!! $path" }
        }else{
            log.debug { " -> File을 S3에서 로드합니다 $path -> $value" }
            val aws = get<AwsClient1>()
            runBlocking {
                aws.s3.getObjectDownload(path.bucket, path.key, value)
            }
        }
        return value
    }

    operator fun setValue(thisRef: File?, property: KProperty<*>, value: File) {
        this.value = value
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}


/**
 * SSM 파라메터 이용
 *  */
fun lazyS3(path: S3Data): S3Property = S3Property(path)