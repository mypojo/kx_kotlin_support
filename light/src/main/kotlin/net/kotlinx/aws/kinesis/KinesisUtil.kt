package net.kotlinx.aws.kinesis

import aws.sdk.kotlin.services.kinesis.model.ProvisionedThroughputExceededException
import net.kotlinx.retry.RetryTemplate
import kotlin.time.Duration.Companion.seconds


object KinesisUtil {

    /** 기본 리트라이 공통설정 */
    val EXCEEDED_RETRY = RetryTemplate {
        predicate = RetryTemplate.match(ProvisionedThroughputExceededException::class.java)
        retries = 10
        interval = 1.seconds
    }

}