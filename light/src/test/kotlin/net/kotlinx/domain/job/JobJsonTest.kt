package net.kotlinx.domain.job

import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import net.kotlinx.domain.job.trigger.JobSerializer
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import java.util.*

class JobJsonTest : BeSpecLight() {

    private val jobSerializer by koinLazy<JobSerializer>()

    init {
        initTest(KotestUtil.IGNORE)

        Given("Job 변환-SQS용 (시리얼라이저 안씀)") {

            /** 베이스 객체 */
            val jobId = UUID.randomUUID().toString()
            val awsId = "975050157771"
            val userId = "1234"
            val job = Job("demoJob","${awsId}#${jobId}") {
                memberId = userId
                jobOption = obj {
                    "inputFilePath" to "s3://bucket/input.txt"
                    "outputFilePath" to "s3://bucket/output.txt"
                }.toGsonData()
                jobEnv = "lambdaJob"
            }

            val jobJson = GsonData.fromObj(job)

            Then("JSON 변환 확인") {
                log.info { "==== job ==== \n${jobJson.toPreety()}" }
            }

            Then("역 변환 확인") {
                val job1 = jobJson.fromJson<Job>()
                job1.jobOption shouldBe job.jobOption
                job1.jobOption shouldBe job.jobOption
                job1.jobEnv shouldBe job.jobEnv
            }

            Then("SQS 전송") {

            }

        }

    }

}
