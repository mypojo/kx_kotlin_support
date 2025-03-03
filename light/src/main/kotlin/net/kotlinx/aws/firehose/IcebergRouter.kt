package net.kotlinx.aws.firehose

import com.google.gson.Gson
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet

/**
 * 아이스버그 적재 스트림 라우터
 *  */
data class IcebergRouter(
    val db: String,
    val table: String,
    val op: String = IcebergOperation.INSERT,
) {

    companion object {
        const val KEY = "route"
    }

    fun wrap(data: Any, gson: Gson = GsonSet.TABLE_UTC): String {
        val body = GsonData.fromObj(data, gson)
        body.put(KEY, GsonData.fromObj(this, gson))  //라우터 정보를 추가해줌
        return body.toString()
    }

}