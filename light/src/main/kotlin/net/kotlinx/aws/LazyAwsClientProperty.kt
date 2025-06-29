package net.kotlinx.aws

import mu.KotlinLogging
import net.kotlinx.koin.Koins
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

/**
 * 설정된 값이 없으면 기본 AWS 로드하는 프로퍼티
 * 코루틴 환경에서도 스레드 세이프하게 동작
 */
class LazyAwsClientProperty {

    /** 코드로 설정된 값 */
    private val setValue = AtomicReference<AwsClient?>(null)

    /** 기본값 캐싱 */
    private val defaultValueCache = AtomicReference<AwsClient?>(null)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): AwsClient {
        // setValue가 있으면 반환
        setValue.get()?.let { return it }
        
        // 캐시된 기본값이 있으면 반환
        defaultValueCache.get()?.let { return it }
        
        // 둘 다 없으면 기본값 생성
        return synchronized(this) {
            // 더블 체크
            defaultValueCache.get() ?: Koins.koin<AwsClient>().also { client ->
                defaultValueCache.set(client)
                log.debug { "디폴트 AwsClient 로드됨.." }
            }
        }
    }

    /** 재할당 해서 초기화 가능하다. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: AwsClient) {
        setValue.set(value)
        defaultValueCache.set(null)  // 할당 해제
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}