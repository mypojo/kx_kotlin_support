package net.kotlinx.aws1.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import aws.sdk.kotlin.services.dynamodb.updateItem
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.toAwsClient1
import net.kotlinx.core2.test.TestLevel03
import net.kotlinx.core2.test.TestRoot

class DynamoDbSupportKtTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @TestLevel03
    fun test() {
        runBlocking {

            aws.dynamo.updateItem {
                this.tableName = "system-dev"
                this.returnValues = ReturnValue.UpdatedNew //새 값 리턴
                this.key = mapOf(
                    DynamoDbBasic.pk to AttributeValue.S("p1"),
                    DynamoDbBasic.sk to AttributeValue.S("s1"),
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