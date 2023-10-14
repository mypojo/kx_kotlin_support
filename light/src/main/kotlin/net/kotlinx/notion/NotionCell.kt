package net.kotlinx.notion

/** 노션 데이터페이스 페이지의 각 항목 컬럼 */
data class NotionCell(
    /** 컬럼 이름 or 블록 ID로 사용됨 */
    val name: String,
    /** 셀 타입 */
    val type: NotionCellType,
    /** 셀의 간단한 텍스트 값 */
    val value: String,
) {

    val notionJson: Any
        get() = type.toNotionJson(value)
}