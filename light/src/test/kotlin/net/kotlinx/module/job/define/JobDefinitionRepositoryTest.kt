package net.kotlinx.module.job.define

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import net.kotlinx.notion.NotionDatabaseToGoogleCalendar
import net.kotlinx.reflect.Bean
import net.kotlinx.test.MyLightKoinStarter
import net.kotlinx.test.TestLight
import net.kotlinx.test.job.NotionDatabaseToGoogleCalendarJob
import org.junit.jupiter.api.Test

class JobDefinitionRepositoryTest : TestLight() {

    init {
        MyLightKoinStarter.startup {
            single {
                log.warn { "모킹객체로 오버라이드 됩니다.." }
                mockk<NotionDatabaseToGoogleCalendar> {
                    every { runBlocking { updateOrInsert() } } answers { println("가짜로 업데이트 했어요") }
                }
            }
        }
    }

    @Test
    fun `mockk 주입 테스트`() {

        val jobDefinition = JobDefinitionUtil.findById(NotionDatabaseToGoogleCalendarJob::class)
        Bean(jobDefinition).toTextGrid().print()

        runBlocking {
            jobDefinition.toJobOption().exe()
        }
    }

}