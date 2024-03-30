package net.kotlinx.core.prop

import net.kotlinx.props.lazyLoadSsm
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class LazyLoadPropertyTest : TestLight() {


    private class MyConfig {
        lateinit var name: String
        var demo: String by lazyLoadSsm("/slack/token")

    }

    @Test
    fun test() {
        val myConfig = MyConfig()
        println(myConfig.demo)

    }
}