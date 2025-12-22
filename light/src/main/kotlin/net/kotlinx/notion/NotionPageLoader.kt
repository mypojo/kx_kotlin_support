package net.kotlinx.notion

import com.lectra.koson.arr
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.core.VibeCoding
import net.kotlinx.json.koson.toGsonData
import org.koin.core.component.KoinComponent

/**
 * 페이지의 블록을 화면에 보이는 순서대로 모두 펼쳐서 단일 리스트로 만들어준다.
 * - 상위 블록을 먼저 추가하고, 자식이 있으면 해당 자식을 순서대로(페이지네이션 순서 유지) 깊이우선으로 이어 붙인다.
 */
@VibeCoding
class NotionPageLoader(secretValue: String) : KoinComponent {

    private val pageClient = NotionPageClient(secretValue)
    private val blockClient = NotionBlockClient(secretValue)

    /**
     * 지정된 페이지의 모든 블록을 "사람이 보는 순서"대로 펼쳐서 반환한다.
     * - 부모 바로 뒤에 자식을 배치하는 깊이우선(DFS) 순서
     * - Notion API 페이지네이션 순서를 그대로 유지
     */
    suspend fun load(pageId: String, pageSize: Int = 100): List<NotionBlock> {
        val result = mutableListOf<NotionBlock>()

        // 제목 가져오기
        val pageInfo = pageClient.retrievePage(pageId)
        val titleProperty = pageInfo["properties"].entryMap().values.find { it["type"].str == "title" }
        val titleText = titleProperty?.let { NotionCell.toText(it) } ?: "Untitled"
        val titleBlockBody = obj {
            "id" to "${pageId}_title"
            "type" to "heading_2"
            "heading_2" to obj {
                "rich_text" to arr[
                    obj {
                        "type" to "text"
                        "plain_text" to titleText
                    }
                ]
            }
        }.toGsonData()
        result += NotionBlock(titleBlockBody, depth = 0)

        // 최상위 블록들을 페이지네이션 순서대로 수집
        pageClient.list(pageId, pageSize).collect { blocks ->
            blocks.forEach { block ->
                // 최상위는 depth 0으로 고정
                val top = NotionBlock(block.body, depth = 0)
                collectRecursively(top, result, 0)
            }
        }
        return result.applyListOrders()
    }

    private suspend fun collectRecursively(block: NotionBlock, acc: MutableList<NotionBlock>, depth: Int) {
        // 현재 블록을 현재 depth로 추가
        acc += if (block.depth == depth) block else NotionBlock(block.body, depth)

        val hasChildren = block.body["has_children"].bool ?: false
        if (!hasChildren) return

        // 자식 블록도 페이지네이션 순서대로 수집하면서 동일하게 전개
        blockClient.list(block.id).collect { children ->
            children.forEach { child ->
                val next = NotionBlock(child.body, depth + 1)
                collectRecursively(next, acc, depth + 1)
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
