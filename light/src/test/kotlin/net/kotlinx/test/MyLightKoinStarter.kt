package net.kotlinx.test

import mu.KotlinLogging
import net.kotlinx.aws.module.batchStep.MyBatchStepModule
import net.kotlinx.module.job.define.MyJobModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/** 해당 패키지의 기본적인 의존성 주입 */
object MyLightKoinStarter {

    private val log = KotlinLogging.logger {}

    /**
     * @param block 테스트용 모킹객체 등 오버라이드 설정용
     * */
    fun startup(profile: String? = null, block: KoinApplication.() -> Unit = {}) {
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
            block()
        }
    }


}