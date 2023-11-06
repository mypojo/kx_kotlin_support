package net.kotlinx.test

import mu.KotlinLogging
import net.kotlinx.test.batchStep.MyBatchStepModule
import net.kotlinx.test.job.MyJobModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object MyLightKoinStarter {

    private val log = KotlinLogging.logger {}

    /**
     * @param block 테스트용 모킹객체 등 오버라이드 설정용
     * */
    fun startup(profile: String? = null, block: Module.() -> Unit = {}) {
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
            modules(module {
                block()
            })

        }
    }


}