package net.kotlinx.core.prop

import net.kotlinx.aws.ssm.lazySsm
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class LazySsmPropertyTest : TestLight(){

    var demo: String by lazySsm()

    @Test
    fun test() {
        demo = "/xx/b"
        println(demo)

    }
}