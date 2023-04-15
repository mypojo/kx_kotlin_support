package net.kotlinx.core2.gson

import com.lectra.koson.arr
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import net.kotlinx.TestRoot
import org.junit.jupiter.api.Test

internal class GsonDataTest : TestRoot() {
    @Test
    fun `기본테스트`() {

        val json = obj {
            "type" to "normal"
            "members" to arr[
                obj { "name" to "A"; "age" to 10; },
                obj { "name" to "B"; "age" to 20; },
            ]
        }
        val updated = GsonData.parse(json).apply {
            put("type", "수정됨")
            put("xxx", "yyy")
            println(this)
        }

        val json2 = obj {
            "option" to  rawJson(updated.toString())
            "optionText" to  updated.toString()
        }

        println(json2.pretty())


        val gsonData = GsonData.parse(json)
        gsonData["members"].filter { it["name"].str == "B" }.onEach { it.put("age", 25) }

        val sumOfAge = gsonData["members"].sumOf { it["age"].long ?: 0L }
        println("sumOfAge : $sumOfAge")

    }
}