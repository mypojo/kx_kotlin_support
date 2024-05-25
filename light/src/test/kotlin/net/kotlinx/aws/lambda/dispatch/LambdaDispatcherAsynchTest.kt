package net.kotlinx.aws.lambda.dispatch

import com.lectra.koson.arr
import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.lambda.dispatch.asynch.SchedulerEvent
import net.kotlinx.aws.lambda.dispatch.asynch.SchedulerEventPublisher
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.kotest.modules.lambdaDispatcher.LambdaDispatcherListener

class LambdaDispatcherAsynchTest : BeSpecLight() {

    private val dispatcher by koinLazy<LambdaDispatcher>()
    private val listener by koinLazy<LambdaDispatcherListener>()

    init {
        initTest(KotestUtil.FAST)

        Given("사전체크") {
            Then("리셋확인") {
                listener.allEvents.size shouldBe 0
            }
        }

        Given("SchedulerEventPublisher 스케줄링") {
            val input = obj {
                SchedulerEventPublisher.DETAIL_TYPE to SchedulerEventPublisher.SCHEDULED_EVENT
                "resources" to arr[
                    "arn:aws:scheduler:ap-northeast-2:99999999:schedule/newGroup/newScheduleName"
                ]
            }

            Then("설정된 스케쥴링으로 이벤트 수신") {
                dispatcher.handleRequest(input)
                listener.allEvents.filterIsInstance<SchedulerEvent>().filter { it.scheduleName == "newScheduleName" }.size shouldBe 1
            }
        }
    }

}
