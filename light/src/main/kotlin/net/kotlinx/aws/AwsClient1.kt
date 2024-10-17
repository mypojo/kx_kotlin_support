package net.kotlinx.aws

import aws.sdk.kotlin.services.athena.AthenaClient
import aws.sdk.kotlin.services.batch.BatchClient
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.ecs.EcsClient
import aws.sdk.kotlin.services.firehose.FirehoseClient
import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.sfn.SfnClient
import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.sts.StsClient
import aws.smithy.kotlin.runtime.client.SdkClient
import net.kotlinx.aws.ssm.SsmStore
import net.kotlinx.reflect.name
import java.util.concurrent.ConcurrentHashMap

/**
 * 기본 AWS 설정
 * http 엔진 설정이 빠져있는데 문제시 적당한거 넣기
 * 중요!!! http 커넥션에서 use 키워드 의미없어보임. 그냥 람다 코루틴에서 오류나는건 리트라이 하자.
 *
 * 성능 최적화 관련
 * 토큰 발급을 백그라운드에서 하면 최적화 가능할듯..
 *
 * 각 모듈들은 별도 로드한다. (jar 제거해도 AwsClient 생성시 오류 안나게)
 *  */
open class AwsClient1(val awsConfig: AwsConfig) {

    /** 클라이언트 보관소 */
    val _cache = ConcurrentHashMap<String, SdkClient>()

    /** 클라이언트 캐시 리턴 */
    inline fun <reified T : SdkClient> getOrCreateClient(crossinline block: () -> T): T {
        return _cache.computeIfAbsent(T::class.name()) {
            block()
        } as T
    }

    //==================================================== 클라이언트 설정 ======================================================
    val firehose: FirehoseClient by lazy { FirehoseClient { awsConfig.build(this) }.regist(awsConfig) }
    val dynamo: DynamoDbClient by lazy { DynamoDbClient { awsConfig.build(this) }.regist(awsConfig) }
    val lambda: LambdaClient by lazy { LambdaClient { awsConfig.build(this) }.regist(awsConfig) }
    val sts: StsClient by lazy { StsClient { awsConfig.build(this) }.regist(awsConfig) }

    //==================================================== 컴퓨팅 인프라 ======================================================
    val sfn: SfnClient by lazy { SfnClient { awsConfig.build(this) }.regist(awsConfig) }
    val athena: AthenaClient by lazy { AthenaClient { awsConfig.build(this) }.regist(awsConfig) }
    val batch: BatchClient by lazy { BatchClient { awsConfig.build(this) }.regist(awsConfig) }
    val ecs: EcsClient by lazy { EcsClient { awsConfig.build(this) }.regist(awsConfig) }

    /** 저장소 */
    val ssm: SsmClient by lazy { SsmClient { awsConfig.build(this) }.regist(awsConfig) }

    //==================================================== 기타 ======================================================
    /** SSM(Systems Manager) 스토어. = 파라메터 스토어 */
    val ssmStore: SsmStore by lazy { SsmStore(ssm) }

}