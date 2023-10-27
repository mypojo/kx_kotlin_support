package net.kotlinx.notion

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.core.collection.repeatCollectUntil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.koson.addByType
import net.kotlinx.core.string.toLocalDateTime
import net.kotlinx.core.time.TimeStart
import net.kotlinx.okhttp.await
import okhttp3.OkHttpClient


/**
 * 노션 DB
 * 각 기능은 필요할때 만들기
 *
 * 데이터베이스 ID 채번 후 해당 페이지로 가서 "연결" 을 선택 후 KEY를 채번한것과 연결 해주어야 한다.
 *  */
class NotionDatabaseClient(
    private val client: OkHttpClient,
    /** 영구키임!! 주의! */
    private val secretValue: String,
) {

    private val log = KotlinLogging.logger {}

    suspend fun insert(dbId: String, cells: List<NotionCell>) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages"
            method = "POST"
            header = toHeader()
            body = toBody(dbId, cells)
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.trace { " -> notion insert 성공" }
    }

    suspend fun update(dbId: String, pageId: String, cells: List<NotionCell>) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages/${pageId}"
            method = "PATCH"
            header = toHeader()
            body = toBody(dbId, cells)
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.trace { " -> notion update 성공" }
    }

    /** 사실 삭제 아니고 아카이브 */
    suspend fun delete(dbId: String, pageId: String) {
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

                val columns = line["properties"].entryMap().map {
                    try {
                        val type = it.value["type"].str!!
                        val cellType = enumValueOf<NotionCellType>(type)
                        val typeValue = it.value[type]
                        val value = cellType.fromNotionJson(typeValue)
                        log.trace { " -> json $cellType -> $typeValue" }
                        NotionCell(it.key, cellType, value)
                    } catch (e: Exception) {
                        log.warn { " -> $it" }
                        throw e
                    }
                }
                NotionRow(id, createdTime, lastEditedTime, columns)
            }
            log.debug { " -> DB[${dbId}] 데이터로드 ${lines.size}건 -> $start" }
            keep.add(lines)
            resultObj["next_cursor"].str
        }.flatten()
    }

    private fun toBody(dbId: String, cells: List<NotionCell>) = obj {
        "parent" to obj {
            "database_id" to dbId
        }
        "properties" to obj {
            cells.forEach { addByType(it.name, it.notionJson) }
        }
    }

    private fun toHeader() = mapOf(
        "Authorization" to "Bearer $secretValue",
        "Notion-Version" to "2022-06-28",
        "Content-Type" to "application/json",
    )

}