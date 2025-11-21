package net.kotlinx.notion

import net.kotlinx.json.gson.GsonData

/**
 * 노션 페이지 블록의 rich_text 등 단순 셀 표현 (읽기전용)
 * 데이터베이스 전용 모델(NotionDatabasePropertie)과 분리하여 사용함
 */
class NotionBlockPropertie(
    /** 원본 json */
    val body: GsonData,
) {

    /** 블록 내부 아이템 타입(text, mention 등) 또는 강제 타입(child_database 등) */
    val type: String = body["type"].str ?: "unknown"

    /** 셀의 간단한 텍스트 값 */
    val viewText: String = NotionCell.toText(body)

    override fun toString(): String = viewText
}
