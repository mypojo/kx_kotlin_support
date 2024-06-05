package net.kotlinx.kotest.modules

import net.kotlinx.kotest.BeSpecKoin
import net.kotlinx.kotest.modules.job.JobModule
import net.kotlinx.kotest.modules.ktor.KtorModule
import net.kotlinx.kotest.modules.lambdaDispatcher.LambdaDispatcherModule

abstract class BeSpecLight : BeSpecKoin(MODULES) {

    companion object {
        val MODULES = listOf(
            BasicModule.moduleConfig(),
            Aws1Module.moduleConfig(),
            JobModule.moduleConfig(),
            BatchStepModule.moduleConfig(),
            KoinTestModule.moduleConfig(),
            LambdaDispatcherModule.moduleConfig(),
            KtorModule.moduleConfig(),
        )
    }

}