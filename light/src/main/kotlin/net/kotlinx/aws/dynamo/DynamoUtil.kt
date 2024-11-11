package net.kotlinx.aws.dynamo

import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 * ttl은 초단위
 * */
object DynamoUtil {

    /**
     * 지금 기준 X일 이후 지정. TTL은 초단위 이다.
     * 이건 자바용으로 남겨둠
     *  */
    fun ttlFromNow(timeUnit: TimeUnit, interval: Long): Long {
        val ttlSec: Long = timeUnit.toSeconds(interval)
        return System.currentTimeMillis() / 1000 + ttlSec
    }

    /** 지금 기준 X일 이후 지정. TTL은 초단위 이다.  */
    fun ttlFromNow(duration: Duration): Long = System.currentTimeMillis() / 1000 + duration.inWholeSeconds

}