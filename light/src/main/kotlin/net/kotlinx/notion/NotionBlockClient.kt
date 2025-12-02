package net.kotlinx.notion

import com.lectra.koson.Koson
import com.lectra.koson.KosonType
import com.lectra.koson.arr
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
class NotionBlockClient(
    /** 영구키임!! 주의! */
    private val secretValue: String,
) : KoinComponent {

    private val client: OkHttpClient by inject()

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * 해당 페이지의 블록을 페이지네이션 단위(List)로 Flow로 방출한다.
     * - 최대 100건씩 Notion API 페이지네이션을 따라가며 방출한다.
     */
    fun list(blockId: String, pageSize: Int = 100): Flow<List<NotionBlock>> = flow {
        var nextToken: String? = null
        repeat(100) { // 안전장치
            val base = "https://api.notion.com/v1/blocks/${blockId}/children?page_size=${pageSize}"
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
     * 단일 블록 조회 (원시 응답을 NotionBlock으로 변환)
     */
    suspend fun get(blockId: String): NotionBlock {
        val resp = client.await {
            url = "https://api.notion.com/v1/blocks/${blockId}"
            method = "GET"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        return NotionBlock(resp.respText.toGsonData())
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
     * table_row 블록의 셀 전체를 교체한다. (부분 업데이트 미지원)
     * contents 크기는 테이블의 컬럼 수와 동일해야 한다.
     */
    suspend fun updateTableRow(rowBlockId: String, contents: List<String>) {
        val resp = client.await {
            url = "https://api.notion.com/v1/blocks/${rowBlockId}"
            method = "PATCH"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            body = obj {
                "table_row" to obj {
                    val cellsList: List<KosonType> = contents.map { text ->
                        arr[
                            obj {
                                "type" to "text"
                                "text" to obj { "content" to text }
                            }
                        ]
                    }
                    "cells" to arr[cellsList]
                }
            }
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.info { "table_row(${rowBlockId}) 업데이트 완료" }
    }

    /**
     * table_row 를 조회 후, 기존값을 기반으로 업데이트 하는 로직
     * @param block  기존 값을 넘겨받아서 신규 값으로 업데이트
     */
    suspend fun updateTableRow(rowBlockId: String, block: (List<String>) -> List<String>) {
        val block = get(rowBlockId)
        check(block.type == "table_row") { "table_row 블록이 아님: ${block.type}" }

        val current = block.body["table_row"]["cells"].map { cell ->
            // 각 셀은 배열로 오며, 첫 요소의 plain_text를 사용
            val first = cell.firstOrNull()
            first?.let { NotionCell.toText(it) } ?: ""
        }
        val newValue = block(current)
        updateTableRow(rowBlockId, newValue)
    }

    /**
     * 페이지(=부모 블록) 안에 자식 블록들을 추가한다.
     * - Notion API: PATCH /v1/blocks/{block_id}/children
     * - 본 메서드는 람다 블록 대신 임의의 바디(Any)를 직접 받는다. 호출부에서 Notion 스펙에 맞는 전체 JSON을 구성해 전달해야 한다.
     *   예) children 배열을 직접 구성하여 전달
     *   ```kotlin
     *   insertRow(pageId, obj {
     *       "children" to arr[
     *           obj {
     *               "object" to "block"
     *               "type" to "paragraph"
     *               "paragraph" to obj {
     *                   "rich_text" to arr[
     *                       obj { "type" to "text"; "text" to obj { "content" to "안녕" } }
     *                   ]
     *               }
     *           }
     *       ]
     *   })
     *   ```
     */
    suspend fun insertTableRow(parentBlockId: String, requestBody: Any) {
        val resp = client.await {
            url = "https://api.notion.com/v1/blocks/${parentBlockId}/children"
            method = "PATCH"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            body = requestBody
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
    }

}