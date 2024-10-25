package net.kotlinx.kotest.modules.lambdaDispatcher

import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.LambdaDispatcher
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import org.koin.core.module.Module
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object LambdaDispatcherModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {
        single {
            log.debug { "LambdaDispatcher 이벤트 등록.." }
            val eventBus = koin<EventBus>()
            eventBus.register(LambdaDispatcherDefaultListener())
            eventBus.register(LambdaDispatcherAwsEventBridgeListener())
            eventBus.register(LambdaDispatcherAwsEventListener())
            eventBus.register(LambdaDispatcherAwsSnsListener())
            LambdaDispatcher()
        }


    }

}