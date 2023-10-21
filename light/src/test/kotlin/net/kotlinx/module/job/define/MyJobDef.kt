package net.kotlinx.module.job.define

object MyJobDef {

    val JOBS = JobDefinitionRepository()

    val NOTION_DATABASE_TO_GOOGLE_CALENDAR_JOB = JOBS.reg {
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