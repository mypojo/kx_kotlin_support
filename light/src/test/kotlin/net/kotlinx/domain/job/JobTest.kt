package net.kotlinx.domain.job

import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.kotest.modules.job.DemoJob
import net.kotlinx.reflect.Bean
import net.kotlinx.string.print

class JobTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("Job") {

            Then("전체 잡 출력") {
                JobDefinitionRepository.list().print()
            }

            val jobDefinition = JobDefinitionRepository.findById(DemoJob::class)
            Then("잡 단일 조회") {
                Bean(jobDefinition).toTextGrid().print()
            }

            xThen("등록된 잡 실행") {
                jobDefinition.toJobOption().exe()
            }
        }
    }

}