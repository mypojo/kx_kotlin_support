package net.kotlinx.aws

import aws.smithy.kotlin.runtime.client.SdkClient
import net.kotlinx.reflect.name
import java.util.concurrent.ConcurrentHashMap

/**
 * 기본 AWS 설정
 * http 엔진 설정이 빠져있는데 문제시 적당한거 넣기
 * 중요!!! http 커넥션에서 use 키워드 의미없어보임. 그냥 람다 코루틴에서 오류나는건 리트라이 하자.
 *
 * 성능 최적화 관련
 * 토큰 발급을 백그라운드에서 하면 최적화 가능할듯..
 *
 * 각 모듈들은 별도 로드한다. (jar 제거해도 AwsClient 생성시 오류 안나게)
 * import에 별도 SDK가 포함되지 않도록 주의하자
 *  */
class AwsClient(val awsConfig: AwsConfig) {

    /** 클라이언트 보관소 (reified 사용하기때문에 private 불가) */
    val cacheInner = ConcurrentHashMap<String, SdkClient>()

    /** 클라이언트 캐시 리턴 */
    inline fun <reified T : SdkClient> getOrCreateClient(crossinline block: () -> T): T {
        return cacheInner.computeIfAbsent(T::class.name()) {
            block()
        } as T
    }

    /** 스토어 보관소 */
    val storeinner = ConcurrentHashMap<String, Any>()

    /** 스토어 캐시 리턴 */
    inline fun <reified T : Any> getOrCreateStore(crossinline block: () -> T): T {
        return storeinner.computeIfAbsent(T::class.name()) {
            block()
        } as T
    }

}