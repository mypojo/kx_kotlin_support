package net.kotlinx.kotest.modules

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.lambda.dispatch.synch.BatchStepDispatcher
import net.kotlinx.domain.batchStep.BatchStepConfig
import net.kotlinx.domain.batchStep.BatchStepExecutor
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
        single { BatchStepDispatcher() }
    }


}