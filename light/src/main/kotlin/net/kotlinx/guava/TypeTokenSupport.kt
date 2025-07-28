package net.kotlinx.guava

import com.google.gson.Gson
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet

/** json을 List 객체로 간단 변환 */
inline fun <reified T> Gson.fromJsonList(json: String): List<T> {
    val type = TypeTokenUtil.list<T>(T::class.java)
    return this.fromJson(json, type)
}

/** json을 List 객체로 간단 변환 */
inline fun <reified T> GsonData.fromJsonList(gson: Gson = GsonSet.GSON): List<T> = gson.fromJsonList<T>(this.toString())