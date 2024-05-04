package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import aws.sdk.kotlin.services.dynamodb.updateItem
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.test.TestLevel03

class DynamoDbSupportKtTest : BeSpecLog() {
    init {
        val aws = AwsConfig(profileName = "sin").toAwsClient1()

        @TestLevel03
        fun test() {
            runBlocking {

                aws.dynamo.updateItem {
                    this.tableName = "system-dev"
                    this.returnValues = ReturnValue.UpdatedNew //새 값 리턴
                    this.key = mapOf(
                        DynamoDbBasic.PK to AttributeValue.S("p1"),
                        DynamoDbBasic.SK to AttributeValue.S("s1"),
                    )
                    this.updateExpression = "set results = :val"
                    this.expressionAttributeValues = mapOf(
                        ":val" to emptyMap<String, String>().toDynamoAttribute()
                    )
                }

                aws.dynamo.updateItemMap("system-dev", "p1", "s1", "results", mapOf("user222" to "11"))
            }

        }
    }
}