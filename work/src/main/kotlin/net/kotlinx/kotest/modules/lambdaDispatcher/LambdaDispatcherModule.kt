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
        single { LambdaDispatcherListener() }
        single {
            log.debug { "LambdaDispatcher 로드.." }
            koin<EventBus>().register(koin<LambdaDispatcherListener>())
            LambdaDispatcher()
        }


    }

}