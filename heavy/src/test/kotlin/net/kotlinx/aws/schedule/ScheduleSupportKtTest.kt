package net.kotlinx.aws.schedule

import aws.sdk.kotlin.services.scheduler.model.ActionAfterCompletion
import aws.sdk.kotlin.services.scheduler.model.ScheduleState
import net.kotlinx.aws.AwsClient
import net.kotlinx.json.gson.json
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.DeploymentType
import net.kotlinx.time.DayOfWeek
import java.time.LocalDateTime

class ScheduleSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("ScheduleSupport.kt") {

            Then("스케쥴 단일 로드") {
                val resp = aws.schedule.getSchedule("aa","--")
                println(resp)
            }

            Then("스케쥴 전체 로드") {
                val allSchedules = aws.schedule.listAllScheduleDetails("my-group")
                allSchedules.printSimple()
            }

            val schedule = SchedulerForLambda(
                groupName = "my-group2",
                name = "my-scheduleJob-aaa-1235",
                cron = CronExpression {
                    configHours = listOf(6, 11, 17)
                    configDaysOfWeek = listOf(DayOfWeek.SUN, DayOfWeek.SAT)
                },
                lambdaName = "${findProfile97}-job-dev",
                lambdaRole = "app-admin",
                dlq = "${findProfile97}-dlq-prod",
                description = "테스트 데이터",
                state = ScheduleState.Enabled,
                after = ActionAfterCompletion.None,
                startTime = LocalDateTime.now().plusDays(1),
                payload = json {
                    "name" to "테스트실행"
                    "pk" to "pk-test"
                    "sk" to "sk-test"
                },
            )

            Then("스케쥴 생성 or 갱신") {
                aws.schedule.updateOrCreateSchedule(schedule)
            }

            Then("스케쥴 그룹 생성") {
                aws.schedule.createScheduleGroup(
                    schedule.groupName, mapOf(
                        "Project" to findProfile97,
                        "DeploymentType" to DeploymentType.DEV.name,
                    )
                )
            }

            Then("5분뒤에 한번 실행하고 삭제") {
                val oneTime = schedule.copy(
                    after = ActionAfterCompletion.Delete,
                    cron = CronExpression.from(LocalDateTime.now().plusMinutes(5)),
                )
                aws.schedule.updateOrCreateSchedule(oneTime)
            }

        }
    }

}
