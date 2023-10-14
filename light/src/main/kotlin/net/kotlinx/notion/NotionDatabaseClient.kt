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
 *  */
class NotionDatabaseClient(
    private val client: OkHttpClient,
    /** 영구키임!! 주의! */
    private val secretValue: String,
) {

    private val log = KotlinLogging.logger {}

    suspend fun update(dbId: String, pageId: String, cells: List<NotionCell>) {
        val resp = client.await {
            url = "https://api.notion.com/v1/pages/${pageId}"
            method = "PATCH"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            body = obj {
                "parent" to obj {
                    "database_id" to dbId
                }
                "properties" to obj {
                    cells.forEach { addByType(it.name, it.notionJson) }
                }
            }
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }
        log.debug { " -> notion update 성공" }
    }

    /** 데이터베이스 쿼리  */
    suspend fun queryAll(dbId: String, filter: ObjectType? = null): List<NotionRow> {
        return repeatCollectUntil { keep, nextToken ->

            val start = TimeStart()
            val resp = client.await {
                url = "https://api.notion.com/v1/databases/${dbId}/query"
                method = "POST"
                header = mapOf(
                    "Authorization" to "Bearer $secretValue",
                    "Notion-Version" to "2022-06-28",
                    "Content-Type" to "application/json",
                )
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

}