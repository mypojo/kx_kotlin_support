package net.kotlinx.module.job.define


import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobTasklet

/**
 * 노션 -> 구글캘린더 동기회
 */
class NotionDatabaseToGoogleCalendarJob : JobTasklet {

    override fun doRun(job: Job) {
        println("=== 테스트 === ")
    }

}
