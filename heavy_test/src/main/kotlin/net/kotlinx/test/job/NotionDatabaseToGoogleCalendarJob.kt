package net.kotlinx.test.job


import kotlinx.coroutines.runBlocking
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobTasklet
import net.kotlinx.notion.NotionDatabaseToGoogleCalendar
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
