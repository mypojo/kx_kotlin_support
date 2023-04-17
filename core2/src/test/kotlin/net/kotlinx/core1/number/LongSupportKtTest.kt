package net.kotlinx.core1.number

import net.kotlinx.core1.time.toKr01
import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class LongSupportKtTest : TestRoot(){

    @Test
    fun test() {

        println(1681869805.seconds.inWholeMilliseconds.toLocalDateTime().toKr01())


    }

}