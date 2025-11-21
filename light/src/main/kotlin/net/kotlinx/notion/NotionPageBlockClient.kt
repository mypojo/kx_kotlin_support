package net.kotlinx.notion

import com.lectra.koson.Koson
import com.lectra.koson.obj
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.okhttp.await
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * 노션 블록 조회
 * 일단 paragraph 만 고려함
 * 각 기능은 필요할때 만들기
 *  */
class NotionPageBlockClient(
    /** 영구키임!! 주의! */
    private val secretValue: String,
) : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val client: OkHttpClient by inject()

    /**
     * 해당 페이지의 블록을 페이지네이션 단위(List)로 Flow로 방출한다.
     * - 최대 100건씩 Notion API 페이지네이션을 따라가며 방출한다.
     */
    fun blocks(pageId: String, pageSize: Int = 100): Flow<List<NotionBlock>> = flow {
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
     * 해당 페이지의 블록 텍스트 업데이트
     * 향후 더 필요시 모듈화
     *  */
    suspend fun updateParagraph(blociId: String, block: Koson.() -> Unit) {  //paragraph: () -> ObjectType,
        val resp = client.await {
            url = "https://api.notion.com/v1/blocks/${blociId}" //페이지 사이즈 고정
            method = "PATCH"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            body = obj {
                "paragraph" to obj {
                    apply(block)
                }
            }
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
    }

    /**
     * 페이지(=부모 블록) 안에 자식 블록들을 추가한다.
     * - Notion API: PATCH /v1/blocks/{block_id}/children
     * - 본 메서드는 "body 자체"를 입력받는다. 즉, 호출부에서 Notion 스펙에 맞는 전체 JSON을 구성해야 한다.
     *   예) children 배열을 직접 구성하여 전달
     *   ```kotlin
     *   client.appendChildrenRaw(pageId) {
     *       "children" to com.lectra.koson.arr {
     *           obj {
     *               "object" to "block"
     *               "type" to "paragraph"
     *               "paragraph" to obj {
     *                   "rich_text" to com.lectra.koson.arr {
     *                       obj { "type" to "text"; "text" to obj { "content" to "안녕" } }
     *                   }
     *               }
     *           }
     *       }
     *   }
     *   ```
     */
    suspend fun appendChildrenRaw(parentBlockId: String, bodyBuilder: Koson.() -> Unit) {
        val resp = client.await {
            url = "https://api.notion.com/v1/blocks/${parentBlockId}/children"
            method = "PATCH"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            body = obj {
                // 호출자가 전체 바디를 구성하도록 위임
                apply(bodyBuilder)
            }
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
    }

}