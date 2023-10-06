package net.kotlinx.notion

import java.time.LocalDateTime

/** 노션 데이터베이스 라인(페이지) */
data class NotionRow(
    val id:String,
    val createdTime: LocalDateTime,
    val lastEditedTime: LocalDateTime,
    val colimns: List<NotionCell>,
)