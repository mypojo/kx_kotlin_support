package net.kotlinx.core1.time

import net.kotlinx.core1.number.toTimeString
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class TimeStringTest {

    @Test
    fun test() {
        val toMillis = TimeUnit.HOURS.toMillis(3) + 2187367
        check(toMillis.toTimeString().toString().contains("3시간"))
    }

    @Test
    fun `시작종료체크`() {
        val start = TimeStart()
        Thread.sleep(2000)
        check(start.toString().contains("2.0초"))
    }

    @Test
    fun `시간변환`() {

        check(61.seconds.inWholeMilliseconds == TimeUnit.SECONDS.toMillis(61))
        check(61450.milliseconds.inWholeSeconds == TimeUnit.MILLISECONDS.toSeconds(61450))

    }

}