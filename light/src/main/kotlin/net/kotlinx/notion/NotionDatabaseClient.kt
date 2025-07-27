package net.kotlinx.notion

import com.lectra.koson.KosonType
import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.collection.repeatCollectUntil
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.rawKeyValue
import net.kotlinx.okhttp.await
import net.kotlinx.string.toLocalDateTime
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
 *  */

@Deprecated("klibnotion 쓰세요")
class NotionDatabaseClient(
    /** 영구키임!! 주의! */
    private val secretValue: String,
) : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val client: OkHttpClient by inject()

    suspend fun insert(dbId: String, cells: List<NotionCell2>) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages"
            method = "POST"
            header = toHeader()
            body = toBody(dbId, cells)
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.trace { " -> notion insert 성공" }
    }

    suspend fun insert(dbId: String, cells: Map<String, KosonType>) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages"
            method = "POST"
            header = toHeader()
            body = toBody(dbId, cells)
            println(body)
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.trace { " -> notion insert 성공" }
    }

    suspend fun update(dbId: String, pageId: String, cells: List<NotionCell2>) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages/${pageId}"
            method = "PATCH"
            header = toHeader()
            body = toBody(dbId, cells)
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

    /** 데이터베이스 쿼리  */
    suspend fun queryAll(dbId: String, filter: ObjectType? = null): List<NotionRow> {
        return repeatCollectUntil { keep, nextToken ->

            val start = TimeStart()
            val resp = client.await {
                url = "https://api.notion.com/v1/databases/${dbId}/query"
                method = "POST"
                header = toHeader()
                body = obj {
                    filter?.let { "filter" to it }
                    nextToken?.let { "start_cursor" to it }
                    "page_size" to 100 //이게 최대임
                }
            }

            check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }

            val resultObj = GsonData.parse(resp.respText)
            val lines = resultObj["results"].map { line ->

                val id = line["id"].str!!
                val createdTime = line["created_time"].str!!.toLocalDateTime().plusHours(9)
                val lastEditedTime = line["last_edited_time"].str!!.toLocalDateTime().plusHours(9)

                val columns = line["properties"].entryMap().map { it.key to NotionCell(it.value) }.toMap()
                NotionRow(id, createdTime, lastEditedTime, columns)
            }
            log.debug { " -> DB[${dbId}] 데이터로드 ${lines.size}건 -> $start" }
            keep.add(lines)
            resultObj["next_cursor"].str
        }.flatten()
    }

    private fun toBody(dbId: String, cells: Map<String, KosonType>) = obj {
        "parent" to obj {
            "database_id" to dbId
        }
        "properties" to obj {
            cells.forEach { rawKeyValue(it.key, it.value) }
        }
    }

    private fun toBody(dbId: String, cells: List<NotionCell2>) = obj {
        "parent" to obj {
            "database_id" to dbId
        }
        "properties" to obj {
            cells.forEach { rawKeyValue(it.name, it.notionJson) }
        }
    }

    private fun toHeader() = mapOf(
        "Authorization" to "Bearer $secretValue",
        "Notion-Version" to "2022-06-28",
        "Content-Type" to "application/json",
    )

}