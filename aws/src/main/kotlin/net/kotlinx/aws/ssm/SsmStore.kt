package net.kotlinx.aws.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.getParameter
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * 파라메터 캐시 저장소
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
                    ssmClient.getParameter { this.name = key; this.withDecryption = true }.parameter?.value
                }
            }
        }
    }


}