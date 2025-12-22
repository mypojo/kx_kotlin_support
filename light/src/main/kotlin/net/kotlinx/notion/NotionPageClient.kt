package net.kotlinx.notion

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.okhttp.await
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 노션 페이지 조회/조작
 * 각 기능은 필요할때 확장
 */
class NotionPageClient(
    /** 영구키임!! 주의! */
    private val secretValue: String,
) : KoinComponent {

    private val client: OkHttpClient by inject()

    /**
     * 해당 페이지의 블록을 페이지네이션 단위(List)로 Flow로 방출한다.
     * - 최대 100건씩 Notion API 페이지네이션을 따라가며 방출한다.
     */
    fun list(pageId: String, pageSize: Int = 100): Flow<List<NotionBlock>> = flow {
        var nextToken: String? = null
        repeat(100) { // 안전장치
            val base = "https://api.notion.com/v1/blocks/${pageId}/children?page_size=${pageSize}"
            val urlStr = nextToken?.let { "$base&start_cursor=$it" } ?: base

            val resp = client.await {
                url = urlStr
                method = "GET"
                header = mapOf(
                    "Authorization" to "Bearer $secretValue",
                    "Notion-Version" to "2022-06-28",
                    "Content-Type" to "application/json",
                )
            }

            check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }

            val result = resp.respText.toGsonData()
            val blocks = result["results"].mapNotNull { body -> NotionBlock(body) }
            if (blocks.isNotEmpty()) emit(blocks)

            nextToken = result["next_cursor"].str
            if (nextToken == null) return@flow
        }
    }

    /**
     * 해당 페이지의 상세 정보를 조회한다.
     * - Notion API: GET /v1/pages/{page_id}
     */
    suspend fun retrievePage(pageId: String): GsonData {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages/${pageId}"
            method = "GET"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        return resp.respText.toGsonData()
    }

    /**
     * 해당 페이지 끝에 자식 블록들을 추가한다.
     * - Notion API: PATCH /v1/blocks/{page_id}/children
     * - body 는 호출자가 Notion 스펙에 맞춰 직접 구성한 객체(Any)로 전달한다. (예: Koson obj/arr, Map 등)
     */
    suspend fun insert(pageId: String, body: Any): List<NotionBlock> {
        val resp = client.await {
            url = "https://api.notion.com/v1/blocks/${pageId}/children"
            method = "PATCH"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            this.body = body
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        val result = resp.respText.toGsonData()
        return result["results"].mapNotNull { body -> NotionBlock(body) }
    }
}