package net.kotlinx.awscdk.lambda

import net.kotlinx.aws.AwsConfig
import net.kotlinx.awscdk.toCdk
import net.kotlinx.koin.Koins.koin
import software.amazon.awscdk.services.dynamodb.ITable
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.eventsources.DynamoEventSource
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource
import software.amazon.awscdk.services.sqs.IQueue
import kotlin.time.Duration.Companion.seconds

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

/**
 * DynamoDB 스트림 트리거 붙이기
 * */
fun IFunction.addEventSourceDynamo(table: ITable, block: DynamoEventSource.Builder.() -> Unit = {}) {
    this.addEventSource(
        DynamoEventSource.Builder.create(table)
            .batchSize(100) //람다 1회 호출당 전달수. 디폴트 10
            .maxBatchingWindow(10.seconds.toCdk()) //얼마나 모았다가 실행할지. 디폴트는 null (즉시)
            .startingPosition(StartingPosition.TRIM_HORIZON) //가장 오래된 데이터부터
            .apply(block)
            .build()
    )
}

/**
 * 2025 03 기준 배드락의 경우 alias에 달면 안되고 루트에 줘야한다
 *  */
fun IFunction.addPermissionToBedrockAgent(agentId: String) {
    val awsConfig = koin<AwsConfig>()
    this.addPermission(
        "BedrockAgentInvokePermission_${agentId}",
        Permission.builder()
            .principal(ServicePrincipal("bedrock.amazonaws.com"))
            .action("lambda:InvokeFunction")
            .sourceArn("arn:aws:bedrock:${awsConfig.region}:${awsConfig.awsId}:agent/${agentId}")
            .build()
    )
}
