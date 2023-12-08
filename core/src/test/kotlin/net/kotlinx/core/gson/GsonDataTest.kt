package net.kotlinx.core.gson

import com.lectra.koson.arr
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import net.kotlinx.core.number.halfUp
import net.kotlinx.core.string.print
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

internal class GsonDataTest : TestRoot() {


    @Test
    fun `그리드`() {
        val array = GsonData.array {
            add(GsonData.obj {
                put("a", 12)
                put("b", 33)
            })
            add(GsonData.obj {
                put("a", 12)
                put("b", 234)
            })
            add(GsonData.obj {
                put("a", 121.123.toBigDecimal().halfUp(-1))
                put("b", "xxx")
            })
        }
        array.print()
        array.take(2).print()
    }

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

        log.info { "SNS 메세지 포맷 확인용 로그 $updated" }
        println(updated)
        println(updated.toString())


        val lett01 = updated["type"].lett { "${it}xx" }
        check(lett01 != null)
        val lett02 = updated["type2"].lett { "${it}xx" }
        check(lett02 == null)


        val json2 = obj {
            "option" to rawJson(updated.toString())
            "optionText" to updated.toString()
        }

        println(json2.pretty())


        val gsonData = GsonData.parse(json)
        gsonData["members"].filter { it["name"].str == "B" }.onEach { it.put("age", 25) }

        val sumOfAge = gsonData["members"].sumOf { it["age"].long ?: 0L }
        println("sumOfAge : $sumOfAge")

    }
}