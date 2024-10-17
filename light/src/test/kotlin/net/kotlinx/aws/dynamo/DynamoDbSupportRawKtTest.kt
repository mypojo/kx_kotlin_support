package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.scan
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.dynamo.query.DynamoExpressionSet
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class DynamoDbSupportRawKtTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.IGNORE)

        Given("DDB 로우 API 조회") {

            val aws = koin<AwsClient1>(findProfile97)

            Then("getItem") {

                val resp = aws.dynamo.getItem {
                    this.tableName = "adv-dev"
                    this.consistentRead = false
                    this.key = mapOf(
                        DynamoBasic.PK to AttributeValue.S("aaaa"),
                        DynamoBasic.SK to AttributeValue.S("info"),
                    )
                }
                println(resp.item)
            }

            val exp = DynamoExpressionSet.PkPrefix {
                pk = "NV#adv#"
            }

            Then("scan 필터링") {
                val resp = aws.dynamo.scan {
                    this.tableName = "adv-dev"
                    this.consistentRead = false
                    this.limit = 10
                    this.filterExpression = exp.filterExpression()
                    this.expressionAttributeValues = exp.expressionAttributeValues()
                }
                resp.items!!.forEach { println(it) }

            }


        }
    }

}