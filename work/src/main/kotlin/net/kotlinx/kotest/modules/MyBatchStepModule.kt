package net.kotlinx.kotest.modules

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepExecutor
import net.kotlinx.aws.module.batchStep.stepDefault.BatchStepDefaultRunner
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.MyEnv
import org.koin.core.module.Module
import org.koin.dsl.module

object MyBatchStepModule : KoinModule {

    override fun moduleConfig(): Module = module {
        single {
            val awsConfig = Koins.koin<AwsConfig>()
            BatchStepConfig {
                stateMachineName = "${awsConfig.profileName}-batchStep-${MyEnv.SUFFIX}"
                workUploadBuket = "${awsConfig.profileName}-work-${MyEnv.SUFFIX}"
            }
        }
        single { BatchStepExecutor() }
        single { BatchStepDefaultRunner() }
    }


}