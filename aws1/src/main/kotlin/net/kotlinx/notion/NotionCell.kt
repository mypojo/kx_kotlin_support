package net.kotlinx.notion

import com.lectra.koson.ObjectType

/** 노션 데이터페이스 페이지의 각 항목 컬럼 */
data class NotionCell(
    val name: String,
    val type: NotionCellType,
    val value: String,
) {

    val notionJson: ObjectType
        get() = type.toNotionJson(value)
}