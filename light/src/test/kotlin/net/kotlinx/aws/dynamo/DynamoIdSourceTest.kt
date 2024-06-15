package net.kotlinx.aws.dynamo

import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class DynamoIdSourceTest : BeSpecHeavy() {

    private val profileName by lazy { findProfile28() }
    private val aws by lazy { koin<AwsClient1>(profileName) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("DynamoIdSource") {

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