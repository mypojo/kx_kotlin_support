package net.kotlinx.core.number

import net.kotlinx.core.time.toKr01
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class LongSupportKtTest : TestRoot(){

    @Test
    fun test() {


        println("aaa/asd".removeSuffix("/"))

        println(1681869805.seconds.inWholeMilliseconds.toLocalDateTime().toKr01())


        println((9 / 20+1))
        println((19 / 20+1))
        println((29 / 20+1))
        println((39 / 20+1))
        println((49 / 20+1))
        println((59 / 20+1))


    }

}