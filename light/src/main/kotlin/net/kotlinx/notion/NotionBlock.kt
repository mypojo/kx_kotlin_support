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
    val cells: List<NotionBlockPropertie> by lazy {
        when (type) {
            "heading_1" -> body["heading_1"]["rich_text"].map { NotionBlockPropertie(it) }
            "heading_2" -> body["heading_2"]["rich_text"].map { NotionBlockPropertie(it) }
            "heading_3" -> body["heading_3"]["rich_text"].map { NotionBlockPropertie(it) }

            "paragraph" -> body["paragraph"]["rich_text"].map { NotionBlockPropertie(it) }

            "numbered_list_item" -> body["numbered_list_item"]["rich_text"].map { NotionBlockPropertie(it) }

            "file" -> listOf(NotionBlockPropertie(body["file"]))
            "child_database" -> {
                //강제로 맞춰준다.
                val json = obj {
                    "type" to "child_database"
                    "title" to body["child_database"]["title"].str
                }
                listOf(NotionBlockPropertie(json.toGsonData()))
            }

            else -> throw IllegalArgumentException("알수없는 형식 : $body")
        }
    }

    val cell: NotionBlockPropertie? by lazy { cells.firstOrNull() }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}