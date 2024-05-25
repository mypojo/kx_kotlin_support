package net.kotlinx.notion

import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData

/**
 * 노션 데이터페이스 페이지의 각 항목 컬럼 (읽기전용)
 *  */
data class NotionBlock(

    /** 원본 json */
    val body: GsonData,
) {

    val id: String = body["id"].str!!

    val type: String = body["type"].str!!

    /** 배열로 들어오는 경우가 있음 */
    val cells: List<NotionCell> by lazy {
        when (type) {
            "heading_3" -> body["heading_3"]["rich_text"].map { NotionCell(it) }

            "paragraph" -> body["paragraph"]["rich_text"].map { NotionCell(it) }

            "file" -> listOf(NotionCell(body["file"]))
            "child_database" -> {
                //강제로 맞춰준다.
                val json = obj {
                    "type" to "child_database"
                    "title" to body["child_database"]["title"].str
                }
                listOf(NotionCell(json.toGsonData()))
            }

            else -> throw IllegalArgumentException("알수없는 형식 : $body")
        }
    }

    val cell: NotionCell? by lazy { cells.firstOrNull() }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}