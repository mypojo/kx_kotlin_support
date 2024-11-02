package net.kotlinx.aws.lambda.dispatch

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LambdaDispatcherAsynchTest : BeSpecHeavy() {

    private val dispatcher by koinLazy<LambdaDispatcher>()

    init {
        initTest(KotestUtil.IGNORE)

        Given("SchedulerEventPublisher 스케줄링") {
            val input = obj {
                //SchedulerEventPublisher.DETAIL_TYPE to SchedulerEventPublisher.SCHEDULED_EVENT
                "resources" to arr[
                    "arn:aws:scheduler:ap-northeast-2:99999999:schedule/newGroup/demoJob"
                ]
            }

            Then("설정된 스케쥴링으로 이벤트 수신") {
                dispatcher.handleRequest(input)
            }
        }

        Given("JOB 이벤트 수신") {
            val input = obj {
                "detail-type" to "Job Status Change"
                "source" to "kotlinx.job"
                "detail" to obj {
                    "pk" to "demoJob"
                    "sk" to "12345"
                    "memberId" to "system"
                    "ttl" to 0
                    "jobStatus" to "SUCCEEDED"
                    "jobErrMsg" to "에러발생!! "
                    "persist" to true
                }
            }

            Then("설정된 스케쥴링으로 이벤트 수신") {
                dispatcher.handleRequest(input)
            }
        }


    }

}
