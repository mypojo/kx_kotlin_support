package net.kotlinx.kotest.modules

import net.kotlinx.kotest.BeSpecKoin
import net.kotlinx.kotest.modules.job.JobModule
import net.kotlinx.kotest.modules.ktor.KtorModule
import net.kotlinx.kotest.modules.lambdaDispatcher.LambdaDispatcherModule

/**
 * 거쳐가는 용도임으로 직접 사용 금지
 * */
abstract class BeSpecLight : BeSpecKoin(MODULES) {

    companion object {
        val MODULES = listOf(
            BasicModule.moduleConfig(),
            AwsModule.moduleConfig(),
            ApiAiModule.moduleConfig(),
            JobModule.moduleConfig(),
            DbItemModule.moduleConfig(),
            DbMultiIndexItemModule.moduleConfig(),

            BatchStepModule.moduleConfig(),
            KoinTestModule.moduleConfig(),
            LambdaDispatcherModule.moduleConfig(),
            KtorModule.moduleConfig(),
            BatchTaskModule.moduleConfig(),
        )
    }

}