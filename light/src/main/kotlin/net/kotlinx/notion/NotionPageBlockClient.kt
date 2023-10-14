package net.kotlinx.notion

import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.core.gson.toGsonData
import net.kotlinx.core.koson.addByType
import net.kotlinx.okhttp.fetch
import okhttp3.OkHttpClient


/**
 * 노션 블록 조회
 * 일단 paragraph 만 고려함
 * 각 기능은 필요할때 만들기
 *  */
class NotionPageBlockClient(
    private val client: OkHttpClient,
    /** 영구키임!! 주의! */
    private val secretValue: String,
) {

    private val log = KotlinLogging.logger {}

    /** 해당 페이지의 블록 조회  */
    fun blocks(pageId: String, pageSize: Int = 100): List<NotionCell> {

        val resp = client.fetch {
            url = "https://api.notion.com/v1/blocks/${pageId}/children?page_size=${pageSize}"
            method = "GET"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
        }

        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }

        val result = resp.respText.toGsonData()
        val blocks = result["results"].mapNotNull { body ->
            val blockId = body["id"].str!!
            when (val type = body["type"].str!!) {
                "paragraph" -> {
                    val richTexts = body["paragraph"]["rich_text"]
                    if (richTexts.empty) null else {
                        val cellType = NotionCellType.rich_text
                        val value = cellType.fromNotionJson(body[type][NotionCellType.rich_text.name])
                        NotionCell(blockId, cellType, value)
                    }
                }

                else -> null
            }
        }
        return blocks
    }

    /** 해당 페이지의 블록 조회  */
    fun update(cell: NotionCell) {

        val resp = client.fetch {
            url = "https://api.notion.com/v1/blocks/${cell.name}" //페이지 사이즈 고정
            method = "PATCH"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            body = obj {
                "paragraph" to obj {
                    addByType(cell.type.name, cell.notionJson)
                }
            }
        }

        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
    }

}