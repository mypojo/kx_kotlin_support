package net.kotlinx.core1.string

import net.kotlinx.core2.test.TestLevel01
import net.kotlinx.core2.test.TestRoot

internal class Base62Test : TestRoot() {


    @TestLevel01
    fun `기본테스트`() {
        val value1 = 1231298123791823L
        val text = Base62Util.toBase62(value1)
        val value2 = Base62Util.fromBase62(text)
        check(value1 == value2)
    }

    @TestLevel01
    fun `스트링_숫자`() {

        val t1 = "12345"
        val t2 = "123.45"
        println("$t1 isNumeric : ${t1.isNumeric()}")
        println("$t2 isNumeric : ${t2.isNumeric()}")


        listOf("12345", "123.45").map {
            arrayOf(it, it.isNumeric())
        }.also {
            listOf("텍스트", "결과").toTextGrid(it).print()
        }


    }
}