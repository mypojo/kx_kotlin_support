package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.scan
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.enhancedExp.DbExpressionSet
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class DynamoDbSupportRawKtTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.IGNORE)

        Given("DDB 로우 API 조회") {

            val aws = koin<AwsClient>(findProfile97)

            Then("getItem") {
                val resp = aws.dynamo.getItem {
                    this.tableName = "adv-dev"
                    this.consistentRead = false
                    this.key = mapOf(
                        DbTable.PK_NAME to AttributeValue.S("aaaa"),
                        DbTable.SK_NAME to AttributeValue.S("info"),
                    )
                }
                println(resp.item)
            }

            Then("scan 필터링") {

                val exp = DbExpressionSet.PkPrefix {
                    pk = "NV#adv#"
                }

                val resp = aws.dynamo.scan {
                    this.tableName = "adv-dev"
                    this.consistentRead = false
                    this.limit = 10
                    this.filterExpression = exp.filterExpression()
                    this.expressionAttributeValues = exp.expressionAttributeValues()
                }
                log.debug { "resp.scannedCount ${resp.scannedCount}" }
                resp.items!!.forEach { println(it) }
            }

        }
    }

}