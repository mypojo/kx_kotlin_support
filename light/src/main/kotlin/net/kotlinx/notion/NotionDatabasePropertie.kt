package net.kotlinx.notion

import net.kotlinx.json.gson.GsonData

/**
 * 노션 데이터페이스 페이지의 각 항목 컬럼 (읽기전용)
 * 어디까지 Cell로 볼것인가 = root prop에 type 이 명시된 애들
 * Page & DB 둘다 공통으로 사용됨
 *  */
class NotionDatabasePropertie(val body: GsonData) {

    /** 컬럼 이름 or 블록 ID로 사용됨 */
    val id: String = body["id"].str!!

    /** 셀 타입 */
    val type: String = body["type"].str!!

    /** 셀의 간단한 텍스트 값 */
    val viewText: String = NotionCell.toText(body)

    //==================================================== 특수케이스 ======================================================

    /** 파일 다운로드 URL 링크 (시간제한 있음) */
    fun downloadUrl(): String {
        check(type != "file")
        return body["file"]["url"].str!!
    }

    override fun toString(): String = viewText

}