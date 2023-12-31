package net.kotlinx.core.prop

import net.kotlinx.aws.ssm.lazySsm
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class LazySsmPropertyTest : TestLight() {


    private class MyConfig {
        lateinit var name:String
        var demo: String by lazySsm()
    }

    @Test
    fun test() {
        val config = MyConfig().apply {
            demo = "/slack/token"
        }
        println(config.demo)
        println(config.demo)

    }
}