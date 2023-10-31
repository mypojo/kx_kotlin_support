package net.kotlinx.module.job.trigger

import kotlinx.coroutines.runBlocking
import net.kotlinx.module.job.define.JobDefinitionUtil
import net.kotlinx.module.job.define.NotionDatabaseToGoogleCalendarJob
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class JobTriggerTest : TestLight() {

    @Test
    fun `로컬실행`() {

        runBlocking {

            val def = JobDefinitionUtil.findById(NotionDatabaseToGoogleCalendarJob::class)
            def.toJobOption().exe()
        }


    }

}