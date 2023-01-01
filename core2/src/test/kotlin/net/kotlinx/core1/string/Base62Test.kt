package net.kotlinx.core1.string

import net.kotlinx.TestRoot

import org.junit.jupiter.api.Test

internal class Base62Test : TestRoot(){

    @Test
    fun `기본테스트`(){
        val value1 = 1231298123791823L
        val text = Base62.toBase62(value1)
        val value2 = Base62.fromBase62(text)
        assert(value1 == value2)
    }
}