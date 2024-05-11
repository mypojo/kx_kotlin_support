package net.kotlinx.domain.job

import net.kotlinx.domain.job.define.JobDefinitionUtil
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.kotest.modules.NotionDatabaseToGoogleCalendarJob
import net.kotlinx.reflect.Bean
import net.kotlinx.string.print

class JobTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("Job") {

            Then("전체 잡 출력") {
                JobDefinitionUtil.list().print()
            }

            val jobDefinition = JobDefinitionUtil.findById(NotionDatabaseToGoogleCalendarJob::class)
            Then("잡 단일 조회") {
                Bean(jobDefinition).toTextGrid().print()
            }

            xThen("등록된 잡 실행") {
                jobDefinition.toJobOption().exe()
            }
        }
    }

}