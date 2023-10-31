package net.kotlinx.test

import mu.KotlinLogging
import net.kotlinx.aws.module.batchStep.MyBatchStepModule
import net.kotlinx.module.job.define.MyJobModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/** 해당 패키지의 기본적인 의존성 주입 */
object MyLightKoinStarter {

    private val log = KotlinLogging.logger {}

    fun startup(profile: String? = null) {
        stopKoin() //체크 없이 그냥 스탑해도 됨
        startKoin {
            log.warn { "test kotin [$profile] start.." }
            modules(
                MyLightModule.moduleConfig(),
                MyAws1Module.moduleConfig(profile),
            )
            modules(
                MyJobModule.moduleConfig(),
                MyBatchStepModule.moduleConfig(profile),
            )
        }
    }


}