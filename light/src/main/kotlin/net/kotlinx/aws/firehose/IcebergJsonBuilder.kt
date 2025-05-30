package net.kotlinx.aws.firehose

import com.google.gson.Gson
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet

/**
 * 아이스버그 적재 스트림 라우터
 *  */
data class IcebergJsonBuilder(
    val db: String,
    val table: String,
    val gson: Gson = GsonSet.TABLE_UTC,
) {

    companion object {
        const val KEY = "route"
    }

    /**
     * 실제 입력 데이터에 경로정보를 매핑해줌
     * */
    fun build(data: Any, op: String = IcebergOperation.INSERT): String {
        val body = GsonData.fromObj(data, gson)
        //라우터 정보를 추가해줌
        body.put(KEY, GsonData.obj {
            put("db", db)
            put("table", table)
            put("op", op)
        })
        return body.toString()
    }

}