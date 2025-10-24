package net.kotlinx.domain.job

import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.eventBridge.EventBridgeConfig
import net.kotlinx.aws.eventBridge.event
import net.kotlinx.aws.eventBridge.putEvents
import net.kotlinx.aws.sqs.sendFifo
import net.kotlinx.aws.sqs.sqs
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
            val job = Job("kwdExtractJob", "${awsId}#${jobId}") {
                memberId = userId
                jobOption = obj {
                    "inputFilePath" to "s3://bucket/input.csv"
                    "outputFilePath" to "s3://bucket/output.csv"
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

            Then("이벤트 강제 전송") {

                val jobStatusByAccount = EventBridgeConfig(
                    EventBridgeConfig.byAccount("992365606987", "job_from_ap-prod"),
                    JobEventBridgePublisher.SOURCE,
                    EventBridgeJobStatus.DETAIL_TYPE,
                )

                val jobJson = GsonData.fromObj(job).toString()
                aws97.event.putEvents(jobStatusByAccount, listOf(jobJson))
            }

            Then("SQS 전송") {
                val client by koinLazy<AwsClient>(findProfile99)
                val queueName = AwsConfig.serviceUrl("sqs", "975050157771", "${findProfile97}-job-dev.fifo")
                client.sqs.sendFifo(queueName, "job-test", "")
            }

        }

    }

}
