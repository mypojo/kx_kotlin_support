package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.component.CdkEventBridgeSchedule.Companion.CronKrOptions
import net.kotlinx.core2.gson.GsonSet
import org.junit.jupiter.api.Test

internal class CdkEventBridgeScheduleTest {

    @Test
    fun `기본테스트`() {

        val options = CronKrOptions(krDay = 2, krHour = 3, minute = "00")
        println(options)
        println(GsonSet.TABLE_UTC.toJson(options))
        println(GsonSet.TABLE_UTC.toJson(options.toUtc()))


    }

}