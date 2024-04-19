package net.kotlinx.core.lib

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class SystemUtilTest : TestRoot(){


    @Test
    fun test() {
        println("==== envPrint ====")
        SystemUtil.envPrint()
        println("==== systemPropertyPrint ====")
        SystemUtil.systemPropertyPrint()
        println("==== jvmParamPrint ====")
        SystemUtil.jvmParamPrint()

    }

}