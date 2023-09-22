package net.kotlinx.aws.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * 파라메터 캐시 저장소
 * 일반적인 테이터가 늦은 초기화로 작동할때는 그냥 ssm 쓰면 됨
 * 이거는 자주 참조되는 데이터를 저장하는용도  ex) 글로벌 설정을 10분마다 교체 등..
 *  */
class SsmStore(
    private val ssmClient: SsmClient,
    /** 캐시 구현체. 디폴트로 하루 한번 */
    private val cache: Cache<String, String?> = CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build(),
) {

    /** 캐시값 우선 리턴 */
    operator fun get(key: String): String? {
        synchronized(this) {
            return cache.get(key) {
                runBlocking {
                    ssmClient.find(key)
                }
            }
        }
    }


}