package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.scan
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class DynamoDbSupportRawKtTest : BeSpecHeavy() {

    val profile = findProfile97()

    init {
        initTest(KotestUtil.IGNORE)

        Given("DDB 로우 API 조회") {

            val aws = koin<AwsClient1>(profile)

            Then("getItem") {

                val resp = aws.dynamo.getItem {
                    this.tableName = "adv-dev"
                    this.consistentRead = false
                    this.key = mapOf(
                        DynamoDbBasic.PK to AttributeValue.S("aaaa"),
                        DynamoDbBasic.SK to AttributeValue.S("info"),
                    )
                }
                println(resp.item)

            }

            Then("scan") {

                val exp = DynamoExpressSet.Scan.DynamoExpressPkPrefix("NV#adv#")

                val resp = aws.dynamo.scan {
                    this.tableName = "adv-dev"
                    this.consistentRead = false
                    this.limit = 10
                    this.filterExpression = exp.expression()
                    this.expressionAttributeValues =exp.expressionAttributeValues()
                }
                resp.items!!.forEach {
                    println(it)
                }


            }
        }
    }

}