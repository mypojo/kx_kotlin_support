package net.kotlinx.awscdk.lambda

import software.amazon.awscdk.services.lambda.FunctionUrlAuthType
import software.amazon.awscdk.services.lambda.FunctionUrlOptions
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource
import software.amazon.awscdk.services.sqs.IQueue

/**
 * 함수에 URL 오픈
 * 각 함수별로 다를 수 있으니 별도 지정해서 사용
 *  */
fun IFunction.url(type: FunctionUrlAuthType = FunctionUrlAuthType.NONE) {
    this.addFunctionUrl(FunctionUrlOptions.builder().authType(type).build()) //외부 오픈됨. 알리아스 & 버전 없음!
}


/**
 * SQS 트리거 붙이기
 * */
fun IFunction.addEventSourceSqs(queue: IQueue, block: SqsEventSource.Builder.() -> Unit = {}) {
    this.addEventSource(
        SqsEventSource.Builder.create(queue)
            .batchSize(1) //한번에 하나씩만
            .maxBatchingWindow(null) //얼마나 모았다가 실행할지?
            .maxConcurrency(5) //동시 실행수
            .apply(block)
            .build()
    )
}
