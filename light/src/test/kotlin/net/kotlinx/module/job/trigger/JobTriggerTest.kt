package net.kotlinx.module.job.trigger

import kotlinx.coroutines.runBlocking
import net.kotlinx.core.test.TestRoot
import net.kotlinx.module.job.define.MyJob
import net.kotlinx.module.job.define.MyJobDef
import net.kotlinx.reflect.Bean
import org.junit.jupiter.api.Test

class JobTriggerTest : TestRoot() {

    @Test
    fun `로컬실행`() {

        runBlocking {
            val option = MyJobDef.NOTION_DATABASE_TO_GOOGLE_CALENDAR_JOB.toJobOption()
            val job = MyJob.JOB_CONFIG.jobTrigger.trigger(option)
            Bean(job).toTextGrid().print()
        }


    }

}