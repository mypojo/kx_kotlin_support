package net.kotlinx.core.number

import net.kotlinx.core.time.toKr01
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class LongSupportKtTest : TestRoot(){

    @Test
    fun test() {


        println(1681869805.seconds.inWholeMilliseconds.toLocalDateTime().toKr01())


    }

}