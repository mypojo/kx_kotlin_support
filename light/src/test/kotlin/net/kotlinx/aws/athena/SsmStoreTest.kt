package net.kotlinx.aws.athena

import net.kotlinx.aws.AwsClient1
import net.kotlinx.test.MyLightKoinStarter
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class SsmStoreTest : TestRoot(), KoinComponent {

    @Test
    fun get() {
        MyLightKoinStarter.startup()
        val aws = get<AwsClient1>()
        println(aws.ssmStore["/rds/endpoint/dev"])
    }
}