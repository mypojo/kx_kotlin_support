package net.kotlinx.aws.dynamo

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class DynamoDbExporterKtTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @Test
    fun test() {


    }


}