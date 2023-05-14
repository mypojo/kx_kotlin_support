package net.kotlinx.core2.gson

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kotlinx.core1.lib.ExceptionUtil
import net.kotlinx.core2.test.TestLevel01
import net.kotlinx.core2.test.TestRoot

class Json_변환테스트 : TestRoot() {

    val kson = Json { ignoreUnknownKeys = true }

    @TestLevel01
    fun `koson 변환테스트`() {

        val sample = CustomLogicReqSample("asdsad", listOf("aa", "bb"), null)

        val json = kson.encodeToString(sample)
        println(json)



        val json2 = "{\"name\":\"asdsad\",\"name22\":\"asdsad\",\"datas\":[\"aa\",\"bb\"],\"intervalSec\":null}\n"
        try {
            kson.decodeFromString<CustomLogicReqSample>(json2)
        } catch (e: Exception) {
            println("오류 나야함 ${ExceptionUtil.toString(e)}")
        }

        val jsonList = kson.decodeFromString<CustomLogicReqSample>(json)
        println(jsonList)
        println(jsonList.interval)


    }

}