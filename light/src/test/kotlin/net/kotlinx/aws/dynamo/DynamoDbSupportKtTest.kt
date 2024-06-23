package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class DynamoDbSupportKtTest : BeSpecHeavy() {

    val profile = findProfile97()

    init {
        initTest(KotestUtil.IGNORE)

        Given("DDB 로우 API 조회") {

            Then("getItem") {

                val aws = koin<AwsClient1>(profile)
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
        }
    }

}