package net.kotlinx.aws.sm

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.getSecretValue
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * 파라메터 캐시 저장소
 *  */
class SmStore(
    private val sm: SecretsManagerClient,
    /** 캐시 구현체. 디폴트로 하루 한번 */
    private val cache: Cache<String, String> = CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build(),
) {

    /**
     * 캐시값 우선 리턴
     * 없으면 예외 던짐!!
     *  */
    operator fun get(key: String): String {
        synchronized(this) {
            return cache.get(key) {
                runBlocking {
                    sm.getSecretValue {
                        this.secretId = key
                    }.secretString!!
                }
            }!!
        }
    }

}