package net.kotlinx.core1.time

import net.kotlinx.core1.number.toTimeString
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

internal class TimeStringTest {

    @Test
    fun test() {

        val toMillis = TimeUnit.HOURS.toMillis(3) + 2187367
        check( toMillis.toTimeString().toString().contains("3시간") )


    }

}