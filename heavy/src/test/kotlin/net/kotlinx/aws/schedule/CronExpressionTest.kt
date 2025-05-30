package net.kotlinx.aws.schedule

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.DayOfWeek

class CronExpressionTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("크론 표현식 생성") {

            Then("매일 특정시간") {
                val express = CronExpression {
                    configHours = listOf(8, 15)
                }
                println(express)
            }

            Then("매일 특정시간") {
                val express = CronExpression {
                    minute = "15"
                    configHours = listOf(9, 20)
                    configDaysOfWeek = listOf(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI)
                }
                println(express)
            }

        }
    }

}

