package net.kotlinx.kotest.modules

import kotlinx.coroutines.runBlocking
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobTasklet
import net.kotlinx.domain.job.define.JobExecuteType
import net.kotlinx.domain.job.define.JobScheduleType
import net.kotlinx.domain.job.define.jobReg
import net.kotlinx.domain.job.trigger.JobLocalExecutor
import net.kotlinx.domain.job.trigger.JobSerializer
import net.kotlinx.koin.KoinModule
import net.kotlinx.kotest.MyEnv
import net.kotlinx.notion.NotionDatabaseToGoogleCalendar
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 노션 -> 구글캘린더 동기회
 */
class NotionDatabaseToGoogleCalendarJob : JobTasklet, KoinComponent {

    val notionDatabaseToGoogleCalendar: NotionDatabaseToGoogleCalendar by inject()

    override fun doRun(job: Job) {
        println("=== 테스트 === ")
        runBlocking {
            notionDatabaseToGoogleCalendar.updateOrInsert()
        }
    }

}

object MyJobModule : KoinModule {

    override fun moduleConfig(): Module = module {
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
