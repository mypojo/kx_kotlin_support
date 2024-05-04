package net.kotlinx.aws.athena

import io.kotest.core.annotation.Tags
import io.kotest.koin.KoinExtension
import io.kotest.koin.KoinLifecycleMode
import io.kotest.matchers.shouldNotBe
import net.kotlinx.aws.AwsClient1
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.test.MyAws1Module
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@Tags("L1")
class SsmStoreTest : BeSpecLog(), KoinComponent {

    override fun extensions() = listOf(
        KoinExtension(
            modules = listOf(MyAws1Module.moduleConfig()),
            mockProvider = null,
            mode = KoinLifecycleMode.Root,
        )
    )

    init {

        val aws by inject<AwsClient1>()

        Given("SsmStoreTest") {
            Then("SSM 데이터가 있음") {
                val value = aws.ssmStore["/cdk-bootstrap/hnb659fds/version"]
                value shouldNotBe null
            }
        }
    }
}
