package net.kotlinx.aws.dynamo

import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.BeSpecLight
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class DynamoIdSourceTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT02)

        Given("DynamoIdSource") {

            val aws = koin<AwsClient1>()

            Then("GUID 채번 테스트") {

                val idSource = DynamoIdSource(
                    dynamoDbClient = aws.dynamo,
                    seqTableName = "guid-dev",
                    pkValue = "kotest-demo",
                )

                for (i in 0..3) {
                    log.info { " => [$i] -> 채번된 ID : ${idSource.invoke()}" }
                }
            }
        }


    }

}