package net.kotlinx.aws_cdk.component.sfnv2

import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class CdkSfnTest : TestRoot() {

    class CdkSfn(block: CdkSfn.() -> Unit) {



        init {
            block(this)
        }
        var a: String = "aa"
        lateinit var b:String
    }


    @Test
    fun test() {

        CdkSfn {
            println(this.a)
            a = "bb"
            println(this.a)
            b = "cc"
            println(this.b)
        }.apply {
            println(this.a)
        }

    }
}