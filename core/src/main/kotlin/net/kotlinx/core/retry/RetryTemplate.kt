package net.kotlinx.core.retry

import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.core.exception.KnownException
import net.kotlinx.core.lib.causes
import net.kotlinx.core.lib.toSimpleString
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 스프링 리트라이가 너무 무거워서 간단하게 만들었다.
 * */
class RetryTemplate(block: RetryTemplate.() -> Unit) : suspend (suspend () -> Any?) -> Any? {

    private val log = KotlinLogging.logger {}

    /** 오류 시 기다릴 시간. multiplier 설정은 나중에 하자.   */
    var interval: Duration = 1.seconds

    /** 리트라이 수 */
    var retries: Int = 3

    /** 리트라이 할지 체크 */
    var predicate: (cause: Throwable) -> Boolean = { cause -> cause.causes().any { it is IOException || it is KnownException.ItemRetryException } }

    /** 오류시 로깅 */
    var onError: (tryCnt: Int, e: Throwable) -> Unit = { tryCnt: Int, e: Throwable ->
        log.warn { " => ${e.toSimpleString()} -> 재시도 회차 $tryCnt/$retries " }
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
                    delay(interval)
                    continue
                } else throw e
            }
        }
        throw IllegalStateException()
    }

    init {
        block(this)
    }

    /** 단축표현 */
    override suspend fun invoke(p1: suspend () -> Any?): Any? = withRetry { p1() }

    companion object {

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