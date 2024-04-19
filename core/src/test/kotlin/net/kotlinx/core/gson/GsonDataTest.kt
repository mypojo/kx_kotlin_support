package net.kotlinx.core.gson

import com.lectra.koson.arr
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import net.kotlinx.core.number.halfUp
import net.kotlinx.string.print
import net.kotlinx.test.TestLevel01
import net.kotlinx.test.TestRoot

internal class GsonDataTest : TestRoot() {

    private data class DataClass01(
        val name: String,
    ) {
        lateinit var aa: String

    }

    @TestLevel01
    fun `데이터클래스 변환`() {

        val class01 = DataClass01("aa")
        class01.aa = "데모데이터"

        val json = GsonSet.GSON.toJson(class01)
        log.debug { "json : $json -> 객체 변환" }

        val q2 = GsonData.parse(json).fromJson<DataClass01>()
        check(q2.aa == "데모데이터")

    }


    @TestLevel01
    fun `그리드 출력`() {
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
                put("a", 121238.12149873.toBigDecimal().halfUp(-1).toPlainString())
                put("b", "xxx")
            })
        }
        array.print()

    }

    @TestLevel01
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
        }

        check(updated["type"].str == "수정됨")
        log.info { "json update -> $updated" }

        val lett01 = updated["type"].lett { "${it}xx" }
        check(lett01 != null)
        val lett02 = updated["type2"].lett { "${it}xx" }
        check(lett02 == null)

        val json2 = obj {
            "option" to rawJson(updated.toString())
            "optionText" to updated.toString()
        }.toGsonData()

        check(json2["option"].isObject)
        check(json2["optionText"].isPrimitive)

        val gsonData = GsonData.parse(json)
        gsonData["members"].filter { it["name"].str == "B" }.onEach { it.put("age", 25) }
        check(gsonData["members"].sumOf { it["age"].long ?: 0L } == 35L)

    }
}