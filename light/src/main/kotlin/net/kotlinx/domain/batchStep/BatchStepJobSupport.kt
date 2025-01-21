package net.kotlinx.domain.batchStep

import net.kotlinx.aws.AwsConfig
import net.kotlinx.domain.job.Job
import net.kotlinx.koin.Koins.koin


/** SFN 콘솔 링크 */
val Job.batchStepLink: String?
    get() {
        if (lastSfnId == null) return null
        val awsConfig = koin<AwsConfig>()
        val stepConfig = koin<BatchStepConfig>()
        return awsConfig.sfnConfig.consoleLink(stepConfig.stateMachineName, lastSfnId!!)
    }