package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.dynamo: DynamoDbClient
    get() = getOrCreateClient { DynamoDbClient { awsConfig.build(this) }.regist(awsConfig) }


/** 자주 사용되는 값 */
typealias DynamoMap = Map<String, AttributeValue>