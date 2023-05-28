package net.kotlinx.aws_cdk.component

import org.junit.jupiter.api.Test

internal class CdkEventBridgeScheduleTest {

    @Test
    fun `기본테스트`() {

        val op = CronKrOptions().apply {
            krDay = 2
            krHour = 3
            minute = "00"
        }.updateToUtc()

        check(op.hour == "18")
        println(CronKrOptions.hourToUtc(1, 13, 19))

    }

}