package net.kotlinx.aws

import aws.smithy.kotlin.runtime.client.SdkClient
import net.kotlinx.retry.RetryTemplate


/** 간단 변환 */
@Deprecated("직접 내장객체 사용하게 변경")
fun AwsConfig.toAwsClient(): AwsClient = this.client

/** AWS 기본 리트라이. IO 예외를 간단 처리하기위함 */
var defaultAwsSdkRetry: RetryTemplate = RetryTemplate {}

/**
 * AWS SDK 확장
 * this를 주입받는다.
 * 참고!!  R은 명시적으로 ?가 붙어있지 않지만 null이 와도 된다.
 *  */
suspend fun <T : SdkClient, R> T.with(call: suspend T.() -> R): R = defaultAwsSdkRetry.withRetry { call() }
