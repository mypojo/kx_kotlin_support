package net.kotlinx.aws.lambda.dispatch

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.lambda.dispatch.asynch.SchedulerEventPublisher
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
                SchedulerEventPublisher.DETAIL_TYPE to SchedulerEventPublisher.SCHEDULED_EVENT
                "resources" to arr[
                    "arn:aws:scheduler:ap-northeast-2:99999999:schedule/newGroup/demoJob"
                ]
            }

            Then("설정된 스케쥴링으로 이벤트 수신") {
                dispatcher.handleRequest(input)
            }
        }
    }

}
