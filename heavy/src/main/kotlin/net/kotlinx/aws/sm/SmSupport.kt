package net.kotlinx.aws.sm

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.sm: SecretsManagerClient
    get() = getOrCreateClient { SecretsManagerClient { awsConfig.build(this) }.regist(awsConfig) }


/** 시크릿 매니저 스토어 */
val AwsClient.smStore: SmStore
    get() = getOrCreateStore { SmStore(this.sm) }


