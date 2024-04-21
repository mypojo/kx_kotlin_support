package net.kotlinx.aws.athena

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.koin.KoinExtension
import io.kotest.koin.KoinLifecycleMode
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.test.MyAws1Module
import org.datavec.api.writable.UnsafeWritableInjector.inject
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


class KotestAndKoin : BehaviorSpec(), KoinComponent {

    override fun extensions() = listOf(
        KoinExtension(
            modules = MyAws1Module.moduleConfig(),
            mockProvider = null,
            mode = KoinLifecycleMode.Root,
        )
    )

    private val log = KotlinLogging.logger {}

    init {
        given("기본적인 코테스트 사용법") {
            `when`("예외 사유 적주기") {
                then("간단 메세지") {
                    val aws by inject<AwsClient1>()
                    println(aws.ssmStore["/rds/endpoint/dev"])
                }

            }
        }
    }
}
