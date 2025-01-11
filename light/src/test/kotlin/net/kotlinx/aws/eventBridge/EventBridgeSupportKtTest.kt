package net.kotlinx.aws.eventBridge

import com.lectra.koson.obj
import net.kotlinx.domain.job.EventBridgeJobStatus
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobEventBridgePublisher
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class EventBridgeSupportKtTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.IGNORE)

        Given("커스텀 이벤트 전달") {

            Then("잡 종료 이벤트") {

                val profile = findProfile97
                val job = Job("demoJob", "12345") {
                    jobErrMsg = "에러발생!! "
                    jobStatus = JobStatus.SUCCEEDED
                }
                val config = EventBridgeConfig(
                    "${profile}-dev",
                    JobEventBridgePublisher.SOURCE,
                    EventBridgeJobStatus.DETAIL_TYPE,
                )
                aws97.event.putEvents(config, listOf(GsonData.fromObj(job).toString()))
            }


            Then("SNS") {

                val config = EventBridgeConfig(
                    "${findProfile97}-dev",
                    "${findProfile97}.test",
                    "test01",
                )

                val obj = obj {
                    "name" to "SNS 테스트"
                }
                aws97.event.putEvents(config, listOf(obj.toGsonData().toString()))
            }

            Then("람다") {

                val config = EventBridgeConfig(
                    "${findProfile97}-dev",
                    "${findProfile97}.lambda",
                    "test01",
                )

                val obj = obj {
                    "name" to "람다 테스트"
                }
                aws97.event.putEvents(config, listOf(obj.toGsonData().toString()))
            }

            Then("AWS 이벤트브릿지 -> API데스티네이션 테스트") {

                val config = EventBridgeConfig(
                    "arn:aws:events:ap-northeast-2:992365606987:event-bus/job_from_${findProfile97}-prod",
                    "Job Test",
                    "Job Status Change",
                )

//                val obj = obj {
//                    "name" to "람다 테스트"
//                }
                val jobId = "a5118a77-cb10-4bc0-9c76-3a5a5e926fb5"
                val awsId = "112233" //잡 요청하는 계정 ID
                val userId = "1234"
                val bucket = "xx-new-real-work"
                //val basicDate = TimeFormat.YMD.get()
                val basicDate = "20250103"
                val job = Job("xxJob", "${awsId}#${jobId}") {
                    memberId = userId
                    jobOption = obj {
                        "INPUT" to "s3://${bucket}/upload/${basicDate}/${jobId}/INPUT.csv"
                        "OUTPUT" to "s3://${bucket}/upload/${basicDate}/${jobId}/OUTPUT.csv"
//                KwdExtractService.CHUNK_SIZE to 50
//                KwdExtractService.API_CALL_CHUNK_SIZE to 20
                    }.toGsonData()
                    jobEnv = "lambdaJob"
                    jobStatus = JobStatus.SUCCEEDED
                }
                //aws97.event.putEvents(config, listOf(obj.toGsonData().toString()))
                val json = GsonData.fromObj(job).toString()
                println(json)
                aws97.event.putEvents(config, listOf(json))
            }


        }
    }


}
