package net.kotlinx.notion

import com.lectra.koson.obj
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData

/**
 * 노션 데이터페이스 페이지의 각 항목 컬럼 (읽기전용)
 *  */
data class NotionBlock(

    /** 원본 json */
    val body: GsonData,

    /** 블록의 계층 깊이 (0부터 시작) */
    val depth: Int = 0,
) {

    val id: String = body["id"].str!!

    val type: String = body["type"].str!!

    /**
     * LLM 등에 전송시 토큰을 조금 아껴보려는 용도
     * JSON 에서 불필요한 부분만 제거하고 그대로 리턴함
     * */
    val contents: GsonData
        get() {
            val newBody = GsonData.parse(body.delegate.deepCopy())
            newBody.remove("created_time")
            newBody.remove("last_edited_time")
            newBody.remove("created_by")
            newBody.remove("last_edited_by")
            newBody.remove("archived")
            newBody.remove("in_trash")
            return newBody
        }

    /**
     * LLM 또는 사람이 읽기 위한 뷰 텍스트
     * */
    val viewText: String by lazy {
        val indent = "  ".repeat(depth) // 2칸 들여쓰기
        "${indent}${type} : ${cells.joinToString(" | ") { it.viewText }}"
    }

    /**
     * 배열로 들어오는 경우가 있음
     * ex) list.forEach { v -> println(v.cells.joinToString(" | ") { it.viewText }) }
     *  */
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

            "table_row" -> {
                //특이하게 cell 이 그리드 형태로 되어있어서 first로 가져온다
                body["table_row"]["cells"].mapNotNull { v -> v.firstOrNull()?.let { NotionBlockPropertie(it) } ?: null }
            }

            //==================================================== 빈값 ======================================================
            "table" -> emptyList()
            "divider" -> emptyList()

            else -> throw IllegalArgumentException("알수없는 형식 : $body")
        }
    }

    val cell: NotionBlockPropertie? by lazy { cells.firstOrNull() }


}