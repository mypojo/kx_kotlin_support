package net.kotlinx.kotest.modules.job

import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.define.JobExecuteType
import net.kotlinx.domain.job.define.JobScheduleType
import net.kotlinx.domain.job.define.jobReg
import net.kotlinx.domain.job.trigger.JobLocalExecutor
import net.kotlinx.domain.job.trigger.JobSerializer
import net.kotlinx.koin.KoinModule
import net.kotlinx.kotest.MyEnv
import org.koin.core.module.Module
import org.koin.dsl.module


object JobModule : KoinModule {

    override fun moduleConfig(): Module = module {
        Job.TABLE_NAME = "job-${MyEnv.SUFFIX}"
        single { JobRepository() }
        single { JobSerializer() }
        single { JobLocalExecutor() }

        jobReg {
            jobClass = DemoJob::class
            name = "데모 작업 실행"
            descs = listOf(
                "x분 주기로 동기화",
                "월비용 =  80원",
            )
            jobExecuteType = JobExecuteType.LAMBDA_SYNCH_NOLOG
            jobScheduleType = JobScheduleType.MINUTES
        }
    }

}
