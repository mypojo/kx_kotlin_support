package net.kotlinx.aws1.dynamo

import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.toAwsClient1
import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test

internal class DynamoIdSourceTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @Test
    fun `기본테스트`() {

        val idSource = DynamoIdSource(
            dynamoDbClient = aws.dynamo,
            seqTableName = "guid-dev",
            pkName = "name",
            pkValue = "demo",
        )
        for (i in 0..3) {
            println(idSource.invoke())
        }


    }

}