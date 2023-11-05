package net.kotlinx.test.job

import net.kotlinx.koin.KoinModule
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobRepository
import net.kotlinx.module.job.define.JobExecuteType
import net.kotlinx.module.job.define.JobScheduleType
import net.kotlinx.module.job.define.jobReg
import net.kotlinx.module.job.trigger.JobLocalExecutor
import net.kotlinx.module.job.trigger.JobSerializer
import net.kotlinx.test.MyEnv
import org.koin.core.module.Module
import org.koin.dsl.module

object MyJobModule : KoinModule {

    override fun moduleConfig(option: String?): Module = module {
        Job.TABLE_NAME = "job-${MyEnv.SUFFIX}"
        single { JobRepository() }
        single { JobSerializer() }
        single { JobLocalExecutor() }

        jobReg {
            jobClass = NotionDatabaseToGoogleCalendarJob::class
            name = "노션데이터베이스 페이지 -> 구글 캘린더 동기화"
            comments = listOf(
                "x분 주기로 동기화",
                "월비용 =  80원",
            )
            jobExecuteType = JobExecuteType.LAMBDA_SYNCH_NOLOG
            jobScheduleType = JobScheduleType.MINUTES
        }
    }

}
