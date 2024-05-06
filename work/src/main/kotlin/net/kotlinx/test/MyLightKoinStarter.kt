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

    val MODULES = listOf(
        MyLightModule.moduleConfig(),
        MyAws1Module.moduleConfig(),
        MyJobModule.moduleConfig(),
        MyBatchStepModule.moduleConfig(),
    )

    /**
     * @param block 테스트용 모킹객체 등 오버라이드 설정용
     * */
    fun startup(block: Module.() -> Unit = {}) {
        log.info { "startup .." }
        stopKoin()
        startKoin {
            modules(MODULES)
            modules(module {
                block()
            })
        }
    }


}