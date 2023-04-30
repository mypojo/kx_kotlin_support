package net.kotlinx.core1.regex

import org.junit.jupiter.api.Test

class RegexSetTest {

    @Test
    fun test() {


        val text = "<tag1>ab영감님</tag1>"

        val matchResult = "tag1".toRegex().find(text)!!
        println(matchResult)
        println(matchResult.value)
        println(matchResult.groupValues)


    }

}