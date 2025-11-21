package net.kotlinx.notion

import net.kotlinx.json.gson.GsonData
import net.kotlinx.string.toLocalDateTime
import java.time.LocalDateTime

/**
 * 노션 데이터베이스 라인(페이지)
 * EventBridgeJson 스타일로 원본 JSON(`body`)을 보관하고, 파생 프로퍼티를 제공한다.
 */
data class NotionDatabaseRow(val body: GsonData) {

    /** 노션 오브젝트 타입 (예: page) */
    val `object`: String = body["object"].str!!

    /** 페이지 ID */
    val id: String = body["id"].str!!

    /** 생성 시간 (KST 보정을 위해 +9시간) */
    val createdTime: LocalDateTime = body["created_time"].str!!.toLocalDateTime().plusHours(9)

    /** 마지막 수정 시간 (KST 보정을 위해 +9시간) */
    val lastEditedTime: LocalDateTime = body["last_edited_time"].str!!.toLocalDateTime().plusHours(9)

    /** 데이터베이스의 각 컬럼들 */
    val properties: Map<String, NotionDatabasePropertie> = body["properties"].entryMap().map { it.key to NotionDatabasePropertie(it.value) }.toMap()
}