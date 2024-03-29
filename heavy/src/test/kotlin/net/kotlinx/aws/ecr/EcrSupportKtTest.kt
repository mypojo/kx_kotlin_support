package net.kotlinx.aws.ecr

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

internal class EcrSupportKtTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun `기본테스트`() {

        runBlocking {

            aws.ecr.findAndUpdateTag("sin-job", "prod-2023-03-29_16-56", "prod")

        }

    }


}