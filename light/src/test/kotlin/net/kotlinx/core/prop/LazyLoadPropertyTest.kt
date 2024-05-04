package net.kotlinx.core.prop

import net.kotlinx.kotest.BeSpecLight
import net.kotlinx.props.lazyLoadSsm
import org.junit.jupiter.api.Test

class LazyLoadPropertyTest : BeSpecLight() {


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