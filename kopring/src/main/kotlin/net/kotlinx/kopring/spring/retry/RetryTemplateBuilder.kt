package net.kotlinx.kopring.spring.retry

import mu.KotlinLogging
import net.kotlinx.core1.lib.ExceptionUtil
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.RetryPolicy
import org.springframework.retry.backoff.BackOffPolicy
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.RetryContextCache
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import kotlin.time.Duration

/**
 * 필요한거만 간단하게 적용한 버전
 */
class RetryTemplateBuilder {

    //==================================================== 옵션 설정값 ======================================================

    /** 내장 컨텍스트 */
    val retryContextCache: RetryContextCache? = null

    /**
     * 리트라이 수행 전, 에러가 발생했을때 로깅 / 콜백 등을 설정
     * ex) 토큰 익스파이어 / 토큰 널 예외를 캐치해서 토큰을 갱신해줌
     */
    var retryListeners: List<RetryListener> = listOf(
        /** 기본 설정으로는 간단 로깅 추가 */
        object : RetryListener {
            override fun <T : Any?, E : Throwable?> open(context: RetryContext?, callback: RetryCallback<T, E>?): Boolean = true

            override fun <T : Any?, E : Throwable?> close(context: RetryContext?, callback: RetryCallback<T, E>?, throwable: Throwable?) {}

            override fun <T, E : Throwable?> onError(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable) {
                log.warn { " => 재시도[${retryPolicy.canRetry(context)}] 오류 카운트 ${context.retryCount} : ${ExceptionUtil.toString(throwable)}" }
            }
        }
    )

    //==================================================== 필수 설정값 ======================================================

    /** 기본이 되는 설정  */
    lateinit var retryPolicy: RetryPolicy

    /** 백오프 설정  */
    lateinit var backOffPolicy: BackOffPolicy

    //==================================================== 설정 ======================================================

    /**
     * 최대 X회까지 리트라이를 시도한다. 즉 X번의 에러가 발생하면 리트라이를 중단함
     * ex) PK중복의 경우 DuplicateKeyException 이런거 쓰면 됨
     */
    fun maxAttempts(maxAttempts: Int, vararg clazzs: Class<out Throwable?>): RetryTemplateBuilder {
        retryPolicy = SimpleRetryPolicy(maxAttempts, clazzs.associateWith { java.lang.Boolean.TRUE }, true) //traverseCauses = true 고정. false는 한번도 필요한적 없었음
        return this
    }

    /**
     * 중분 백오프
     * @param backoffMills 밀리초
     */
    fun backoff(duration: Duration): RetryTemplateBuilder {
        val backoffMills = duration.inWholeMilliseconds
        backOffPolicy = ExponentialBackOffPolicy().apply {
            maxInterval = backoffMills * 5 //최대 5배 고정
            initialInterval = backoffMills
            multiplier = 1.2 //걍 1.2 고정
        }
        return this
    }

    //==================================================== 생성 ======================================================

    /** 매번 만들지 않고 재사용한다. 원래 의도가 이런지는 모르겠음  */
    fun build(): RetryTemplate = RetryTemplate().apply {
        setRetryPolicy(retryPolicy)
        setBackOffPolicy(backOffPolicy)
        setRetryContextCache(retryContextCache) //상태가 있는 리트라이를 캐싱해주는 저장소. 커스텀 복구 로직에 사용한다.
        for (retryListener in retryListeners) {
            registerListener(retryListener)
        }
    }


    companion object {

        private val log = KotlinLogging.logger {}

    }
}
