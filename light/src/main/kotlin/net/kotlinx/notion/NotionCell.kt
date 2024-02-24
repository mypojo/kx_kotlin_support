package net.kotlinx.notion

import mu.KotlinLogging
import net.kotlinx.core.gson.GsonData

/**
 * 노션 데이터페이스 페이지의 각 항목 컬럼 (읽기전용)
 * 어디까지 Cell로 볼것인가 = root prop에 type 이 명시된 애들
 * Page & DB 둘다 공통으로 사용됨
 *  */
class NotionCell(
    /** 원본 json. */
    val body: GsonData
) {

    /** 컬럼 이름 or 블록 ID로 사용됨 */
    val id: String
        get() = body["id"].str!!

    /** 셀 타입 */
    val type: String
        get() = body["type"].str!!

    /** 셀의 간단한 텍스트 값 */
    val viewText: String
        get() {
            return when (type) {
                "child_database" -> body["title"].str!!  //강제로 생성한 타입!!
                "text" -> body["plain_text"].str ?: ""
                "title" -> body["title"].joinToString("|") { it["plain_text"].str ?: "xx" }
                "rich_text" -> body["plain_text"].str ?: ""
                //"select" -> body["name"].str ?: ""
                "select" -> body["select"]["name"].str ?: ""
                "multi_select" -> body["multi_select"].joinToString("|") { it["name"].str!! }
                "url" -> body.str!! //그 자체
                "number" -> body.str!! //그 자체
                "checkbox" -> body.str!! //그 자체
                "file" -> body["name"].str!!
                "files" -> body["files"].joinToString("|") { it["name"].str ?: "" }
                "date" -> {
                    if (body["end"].empty) "${body["start"].str}"
                    else "${body["start"].str} ~ ${body["end"].str}"
                }

                else -> body.toPreety()
            }
        }

    //==================================================== 특수케이스 ======================================================

    /** 파일 다운로드 URL 링크 (시간제한 있음) */
    fun downloadUrl(): String {
        check(type != "file")
        return body["file"]["url"].str!!
    }

    override fun toString(): String = viewText

    companion object {

        private val log = KotlinLogging.logger {}
    }

}