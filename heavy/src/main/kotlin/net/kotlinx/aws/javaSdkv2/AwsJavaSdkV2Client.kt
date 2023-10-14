package net.kotlinx.aws.javaSdkv2


import net.kotlinx.aws.AwsConfig
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import kotlin.time.toJavaDuration

/** 선형 구조임으로 조합보다는 상속이 더 좋은 선택 */
class AwsJavaSdkV2Client(awsConfig: AwsConfig) {

    val region = Region.regions().find { it.id() == awsConfig.region }!!

    val credentialsProvider: DefaultCredentialsProvider by lazy { DefaultCredentialsProvider.builder().profileName(awsConfig.profileName).build() }

    /**
     * 이미 있는걸로 적당히 설정한다.
     * connectionTimeToLive 는 설정하지 않음
     *  */
    val httpClient: SdkHttpClient by lazy {
        ApacheHttpClient.builder()
            .maxConnections(awsConfig.httpMaxConnections)
            .connectionMaxIdleTime(awsConfig.connectionIdleTimeout.toJavaDuration())
            .connectionTimeout(awsConfig.httpConnectTimeout.toJavaDuration())
            .socketTimeout(awsConfig.httpSocketWriteTimeout.toJavaDuration()) //이게 맞는지는 모르겠음
            .build()
    }

    val ddb: DynamoDbClient by lazy { DynamoDbClient.builder().region(region).credentialsProvider(credentialsProvider).httpClient(httpClient).build() }


}
