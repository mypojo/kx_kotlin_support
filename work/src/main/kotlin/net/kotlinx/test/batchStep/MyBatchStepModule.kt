package net.kotlinx.test.batchStep

import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepExecutor
import net.kotlinx.aws.module.batchStep.stepDefault.BatchStepDefaultRunner
import net.kotlinx.koin.KoinModule
import net.kotlinx.test.MyEnv
import org.koin.core.module.Module
import org.koin.dsl.module

object MyBatchStepModule : KoinModule {

    override fun moduleConfig(option: String?): Module = module {
        single {
            BatchStepConfig {
                stateMachineName = "${option}-batchStep-${MyEnv.SUFFIX}"
                workUploadBuket = "${option}-work-${MyEnv.SUFFIX}"
            }
        }
        single { BatchStepExecutor() }
        single { BatchStepDefaultRunner() }
    }


}