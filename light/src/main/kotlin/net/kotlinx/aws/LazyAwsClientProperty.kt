package net.kotlinx.aws

import mu.KotlinLogging
import net.kotlinx.koin.Koins
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty


/**
 * 설정된 값이 없으면 기본 AWS 로드하는 프로퍼티
 * 스레드 세이프하지 않음
 * 스래드 세이프 때문에 설정용인 LazyDefaultProperty 를 사용하지 않음
 * @see LazyDefaultProperty
 * */
class LazyAwsClientProperty {

    /** 코드로 설정된 값 */
    @Volatile
    private var setValue: AwsClient? = null

    /** 기본값 캐싱 */
    private val defaultValueCache = AtomicReference<AwsClient?>(null)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): AwsClient {
        return setValue ?: defaultValueCache.get() ?: synchronized(this) {
            //더블 체크 해준다
            defaultValueCache.get() ?: Koins.koin<AwsClient>().also { client ->
                defaultValueCache.set(client)
                log.debug { "디폴트 AwsClient 로드됨.." }
            }
        }
    }

    /** 재할당 해서 초기화 가능하다. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: AwsClient) {
        synchronized(this) {
            defaultValueCache.set(null)  // 할당 해제
            setValue = value
        }
    }

    companion object {

        private val log = KotlinLogging.logger {}

    }

}
