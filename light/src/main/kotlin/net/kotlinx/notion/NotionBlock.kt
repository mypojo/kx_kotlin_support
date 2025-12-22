package net.kotlinx.notion

import com.lectra.koson.obj
import net.kotlinx.core.VibeCoding
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData

/**
 * 노션 데이터페이스 페이지의 각 항목 컬럼 (읽기전용)
 *  */
@VibeCoding
data class NotionBlock(

    /** 원본 json */
    val body: GsonData,

    /** 블록의 계층 깊이 (0부터 시작) */
    val depth: Int = 0,

    /** numbered_list_item 인 경우의 순서 (1부터 시작) */
    val listOrder: Int = 1,

    /** 테이블의 헤더 여부 (마크다운 구분선 출력을 위해 사용) */
    val isTableHeader: Boolean = false,
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
     * 마크다운 텍스트
     * */
    val markdown: String by lazy {
        val indent = "  ".repeat(depth)
        val content = when (type) {
            "table_row" -> cells.joinToString(" | ") { it.viewText.replace("\n", "<br>") }.let { "| $it |" }
            else -> cells.joinToString("") { it.viewText }
        }

        when (type) {
            "heading_1" -> "${indent}# $content"
            "heading_2" -> "${indent}## $content"
            "heading_3" -> "${indent}### $content"
            "paragraph" -> "${indent}$content"
            "numbered_list_item" -> "${indent}${listOrder}. $content"
            "bulleted_list_item" -> "${indent}- $content"
            "to_do" -> {
                val checked = body["to_do"]["checked"].bool == true
                val checkMark = if (checked) "[x]" else "[ ]"
                "${indent}$checkMark $content"
            }

            "quote" -> "${indent}> $content"
            "table_row" -> {
                val row = "${indent}$content"
                if (isTableHeader) {
                    val separator = cells.joinToString(" | ") { "---" }.let { "| $it |" }
                    "$row\n${indent}$separator"
                } else {
                    row
                }
            }

            "divider" -> "${indent}---"
            "table" -> "" // 테이블 컨테이너는 텍스트 없음
            "child_database" -> "${indent}### $content"
            "file" -> "${indent}[file] $content"
            else -> "${indent}$content"
        }
    }

    /**
     * 배열로 들어오는 경우가 있음
     * ex) list.forEach { v -> println(v.mdText) }
     *  */
    val cells: List<NotionBlockPropertie> by lazy {
        when (type) {
            "heading_1" -> body["heading_1"]["rich_text"].map { NotionBlockPropertie(it) }
            "heading_2" -> body["heading_2"]["rich_text"].map { NotionBlockPropertie(it) }
            "heading_3" -> body["heading_3"]["rich_text"].map { NotionBlockPropertie(it) }

            "paragraph" -> body["paragraph"]["rich_text"].map { NotionBlockPropertie(it) }

            "numbered_list_item" -> body["numbered_list_item"]["rich_text"].map { NotionBlockPropertie(it) }
            "bulleted_list_item" -> body["bulleted_list_item"]["rich_text"].map { NotionBlockPropertie(it) }
            "to_do" -> body["to_do"]["rich_text"].map { NotionBlockPropertie(it) }
            "quote" -> body["quote"]["rich_text"].map { NotionBlockPropertie(it) }

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
                body["table_row"]["cells"].map { cellArray ->
                    val text = cellArray.joinToString("") { NotionCell.toText(it) }
                    val fakeBody = obj {
                        "type" to "rich_text"
                        "plain_text" to text
                    }.toGsonData()
                    NotionBlockPropertie(fakeBody)
                }
            }

            //==================================================== 빈값 ======================================================
            "table" -> emptyList()
            "divider" -> emptyList()

            else -> throw IllegalArgumentException("알수없는 형식 : $body")
        }
    }

    val cell: NotionBlockPropertie? by lazy { cells.firstOrNull() }


}