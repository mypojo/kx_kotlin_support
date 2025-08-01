package net.kotlinx.retry

import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.exception.KnownException
import net.kotlinx.exception.causes
import net.kotlinx.exception.toSimpleString
import java.io.IOException
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * 스프링 리트라이가 너무 무거워서 간단하게 만들었다.
 * */
class RetryTemplate {

    private val log = KotlinLogging.logger {}

    @Kdsl
    constructor(block: RetryTemplate.() -> Unit = {}) {
        apply(block)
    }

    /**
     * 구분 가능한 간단 이름.
     * 디폴트 에러에 사용됨
     * */
    var name: String = ""

    /** 오류 시 기다릴 시간. */
    var interval: Duration = 1.seconds

    /** 리트라이 수 */
    var retries: Int = 3

    /** 백오프 배수 - 리트라이마다 대기 시간이 이 값의 배수로 증가함 */
    var backOffMultiplier: Double = 1.2

    /**
     * 리트라이 할지 체크
     * 디폴트로는 IO or 리트라이 예외
     *  */
    var predicate: (cause: Throwable) -> Boolean = { cause -> cause.causes().any { it is IOException || it is KnownException.ItemRetryException } }

    /** 오류시 로깅 */
    var onError: (tryCnt: Int, e: Throwable) -> Unit = { tryCnt: Int, e: Throwable ->
        val title = if (name.isEmpty()) "" else "[$name]"
        log.warn { " => retry $title $tryCnt/$retries => ${e.toSimpleString()}" }
    }

    /** 리트라이 시도 */
    suspend fun <T> withRetry(call: suspend () -> T): T {
        for (i in 0..retries) {
            return try {
                call()
            } catch (e: Exception) {
                val canRetry = predicate(e)
                if (canRetry && i < retries) {
                    onError(i + 1, e)
                    // 백오프 적용: interval * (backOffMultiplier ^ i), 최대 10배로 제한
                    val calculatedDelay = (interval.inWholeMilliseconds * backOffMultiplier.pow(i)).toLong()
                    val maxDelay = interval.inWholeMilliseconds * INTERVAL_LIMIT
                    val delayTime = minOf(calculatedDelay, maxDelay) //최대치 제한
                    delay(delayTime)
                    continue
                } else throw e
            }
        }
        throw IllegalStateException()
    }

    companion object {

        /** x배 이상 늘어나면 제한 */
        private const val INTERVAL_LIMIT = 10

        /** 모은 예외 리트라이 */
        val ALL: (cause: Throwable) -> Boolean = { true }

        /**
         * 특정 예외에 해당되는경우 리트라이
         * ex) RetryTemplate.match(ResourceConflictException::class.java)
         * */
        fun match(vararg targets: Class<*>): (cause: Throwable) -> Boolean = { cause ->
            cause.causes().any { e ->
                targets.any { it.isInstance(e) }
            }
        }
    }

}