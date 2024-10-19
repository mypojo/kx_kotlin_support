import aws.sdk.kotlin.services.athena.AthenaClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist


val AwsClient.athena: AthenaClient
    get() = getOrCreateClient { AthenaClient { awsConfig.build(this) }.regist(awsConfig) }