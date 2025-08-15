package net.kotlinx.aws.schedule

import aws.sdk.kotlin.services.scheduler.model.ActionAfterCompletion
import aws.sdk.kotlin.services.scheduler.model.ScheduleState
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient
import net.kotlinx.json.gson.json
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.DeploymentType
import net.kotlinx.time.DayOfWeek
import java.time.LocalDateTime

class ScheduleSupportKtTest : BeSpecHeavy() {

    private val aws by koinLazy<AwsClient>()

    init {
        initTest(KotestUtil.SLOW)

        Given("ScheduleSupport.kt") {

            val myGroupName = "kx-life-dev"

            Then("스케쥴 단일 로드") {
                val resp = aws.schedule.getSchedule(myGroupName, "notionDatabaseToGoogleCalendarJob-dev")
                resp.actionAfterCompletion shouldBe ActionAfterCompletion.None
            }

            Then("스케쥴 전체 로드") {
                val allSchedules = aws.schedule.listAllScheduleDetails(myGroupName)
                allSchedules.printSimple()
                allSchedules.size shouldBeGreaterThanOrEqual 1
            }

            val schedule = SchedulerForLambda(
                groupName = myGroupName,
                name = "demo",
                cron = CronExpression {
                    configHours = listOf(6, 11, 17)
                    configDaysOfWeek = listOf(DayOfWeek.SUN, DayOfWeek.SAT)
                },
                lambdaName = "${findProfile97}-job-dev",
                lambdaRole = "app-admin",
                dlq = "${findProfile97}-dlq-prod",
                description = "테스트 데이터",
                state = ScheduleState.Disabled,
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

            Then("스케쥴 삭제") {
                aws.schedule.deleteSchedule(schedule.groupName, schedule.name)
            }

            xThen("스케쥴 그룹 생성") {
                aws.schedule.createScheduleGroup(
                    schedule.groupName, mapOf(
                        "Project" to findProfile97,
                        "DeploymentType" to DeploymentType.DEV.name,
                    )
                )
            }

            xThen("5분뒤에 한번 실행하고 삭제") {
                val oneTime = schedule.copy(
                    after = ActionAfterCompletion.Delete,
                    cron = CronExpression.from(LocalDateTime.now().plusMinutes(5)),
                )
                aws.schedule.updateOrCreateSchedule(oneTime)
            }

        }
    }

}
