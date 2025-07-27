package net.kotlinx.notion

import com.lectra.koson.Koson
import com.lectra.koson.obj
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
@Deprecated("klibnotion 쓰세요")
class NotionPageBlockClient(
    /** 영구키임!! 주의! */
    private val secretValue: String,
) : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val client: OkHttpClient by inject()

    /** 해당 페이지의 블록 조회  */
    suspend fun blocks(pageId: String, pageSize: Int = 100): List<NotionBlock> {

        val resp = client.await {
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
            try {
                NotionBlock(body)
            } catch (e: IllegalArgumentException) {
                log.warn { "알수없는 형식 : $body" }
                null
            }
        }
        return blocks
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

}