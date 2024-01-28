package net.kotlinx.core.number

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class NumberUtilTest : TestRoot(){


    @Test
    fun test() {

        var size = 16
        var cycle = 3
        val resize = size / cycle

        log.info { "사이즈 $size"  }

        (0 until size).forEach {

            log.debug { "$it ${it % resize}" }

        }

    }

}