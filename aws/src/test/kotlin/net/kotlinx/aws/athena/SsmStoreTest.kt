package net.kotlinx.aws.athena

import net.kotlinx.TestRoot
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import org.junit.jupiter.api.Test

internal class SsmStoreTest :TestRoot() {

    @Test
    fun get() {

        val aws = AwsConfig(profileName = "sin").toAwsClient()
        println(aws.ssmStore["/rds/endpoint/dev"])


    }
}