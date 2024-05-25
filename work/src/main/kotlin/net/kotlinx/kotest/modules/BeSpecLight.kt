package net.kotlinx.kotest.modules

import net.kotlinx.kotest.BeSpecKoin
import net.kotlinx.kotest.modules.job.JobModule
import net.kotlinx.kotest.modules.ktor.KtorModule
import net.kotlinx.kotest.modules.lambdaDispatcher.LambdaDispatcherModule

abstract class BeSpecLight : BeSpecKoin(MODULES) {

    companion object {
        val MODULES = listOf(
            MyBasicModule.moduleConfig(),
            MyAws1Module.moduleConfig(),
            JobModule.moduleConfig(),
            MyBatchStepModule.moduleConfig(),
            MyKoinTestModule.moduleConfig(),
            LambdaDispatcherModule.moduleConfig(),
            KtorModule.moduleConfig(),
        )
    }

}