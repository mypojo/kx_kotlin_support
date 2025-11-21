package net.kotlinx.notion

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.okhttp.await
import net.kotlinx.time.TimeStart
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * 노션 DB
 * 각 기능은 필요할때 만들기
 * 데이터베이스 ID 채번 후 해당 페이지로 가서 "연결" 을 선택 후 KEY를 채번한것과 연결 해주어야 한다.
 *
 * => 바닐라 rest 구현 지양하기!!
 * klibnotion 가 업데이트 안되서 걍 rest로 구현함
 *  */
class NotionDatabaseClient(
    /** 영구키임!! 주의! */
    private val secretValue: String,
) : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val client: OkHttpClient by inject()

    /**
     * 데이터베이스 쿼리
     * - 각 페이지네이션 결과(최대 100건)를 리스트로 묶어 순차적으로 Flow로 방출한다.
     * - sorts 파라미터를 지정하지 않거나 null일 경우, 기본 정렬 순서는 마지막 편집 시간(Last Edited Time) 내림차순(Descending)이 일반적이다.
     */
    fun query(dbId: String, filter: ObjectType? = null): Flow<List<NotionDatabaseRow>> = flow {
        var nextToken: String? = null
        repeat(100) { // 안전장치
            val start = TimeStart()
            val resp = client.await {
                url = "https://api.notion.com/v1/databases/${dbId}/query"
                method = "POST"
                header = toHeader()
                body = obj {
                    filter?.let { "filter" to it }
                    nextToken?.let { "start_cursor" to it }
                    "page_size" to 100 // 100개가 최대임
                }
            }

            check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }

            val resultObj = GsonData.parse(resp.respText)
            val lines = resultObj["results"].map { line -> NotionDatabaseRow(line) }
            log.debug { " -> DB[${dbId}] 데이터로드 ${lines.size}건 -> $start" }
            if (lines.isNotEmpty()) emit(lines)

            nextToken = resultObj["next_cursor"].str
            if (nextToken == null) return@flow
        }
    }

    suspend fun insert(dbId: String, properties: ObjectType) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages"
            method = "POST"
            header = toHeader()
            body = toBody(dbId, properties)
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.trace { " -> notion insert 성공" }
        println(resp.respText)
    }

    suspend fun update(dbId: String, pageId: String, properties: ObjectType) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages/${pageId}"
            method = "PATCH"
            header = toHeader()
            body = toBody(dbId, properties)
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.trace { " -> notion update 성공" }
    }

    /**
     * 사실 삭제 아니고 아카이브
     * dbId 가 필요하지 않는게 맞는지 확인 필요!!
     *  */
    suspend fun delete(pageId: String) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages/${pageId}"
            method = "PATCH"
            header = toHeader()

            body = obj {
                "archived" to true
            }
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.trace { " -> notion delete 성공" }
    }

    private fun toBody(dbId: String, properties: ObjectType) = obj {
        "parent" to obj {
            "database_id" to dbId
        }
        "properties" to properties
    }

    private fun toHeader() = mapOf(
        "Authorization" to "Bearer $secretValue",
        "Notion-Version" to "2022-06-28",
        "Content-Type" to "application/json",
    )

}