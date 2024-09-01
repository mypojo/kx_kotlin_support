package net.kotlinx.aws.schedule

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class ScheduleSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("ScheduleSupport.kt") {

            Then("스케쥴 전체 로드") {
                val allSchedules = aws.schedule.listAllScheduleDetails()
                allSchedules.printSimple()
            }
        }
    }

}
