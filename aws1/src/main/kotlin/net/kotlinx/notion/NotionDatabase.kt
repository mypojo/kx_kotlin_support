package net.kotlinx.notion

import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.okhttp.fetch
import net.kotlinx.core.collection.repeatCollectUntil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.string.toLocalDateTime
import net.kotlinx.core.time.TimeStart
import okhttp3.OkHttpClient

/**
 * 노션 DB
 * 각 기능은 필요할때 만들기
 *  */
class NotionDatabase(
    val client: OkHttpClient,
    /** 영구키임!! 주의! */
    private val secretValue: String,
    val dbId: String,
) {

    private val log = KotlinLogging.logger {}

    /** 최대 100건씩 X번만 불러옴 */
    var maxFetchCnt = 10

    suspend fun update(pageId: String) {
        val resp = client.fetch {
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
                    "gcId" to obj {
                        "type" to "rich_text"
                        "rich_text" to arr[
                            obj {
                                "text" to obj {
                                    "content" to "xxxx"
                                }
                            }
                        ]
                    }
                }

            }
        }
        check(resp.response.code == 200) { "${resp.response.code} ${resp.respText}" }

        println(resp.respText)

    }

    /** 데이터베이스 쿼리  */
    suspend fun queryAll(filter: ObjectType? = null): List<NotionRow> {
        return repeatCollectUntil { keep, nextToken ->

            val start = TimeStart()
            val resp = client.fetch {
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
                    val type = it.value["type"].str!!
                    val cellType = enumValueOf<NotionCellType>(type)
                    val value = cellType.fromNotionJson(it.value[type])
                    log.debug { " -> json $cellType -> ${it.value[type]}" }
                    NotionCell(it.key, cellType, value)
                }
                NotionRow(id, createdTime, lastEditedTime, columns)
            }
            log.debug { " -> DB[${dbId}] 데이터로드 ${lines.size}건 -> $start" }
            keep.add(lines)
            resultObj["next_cursor"].str
        }.flatten()
    }

}