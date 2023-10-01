package net.kotlinx.aws

import com.lectra.koson.arr
import com.lectra.koson.obj
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.okhttp.fetch
import net.kotlinx.core.gson.GsonData
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test

internal class Notion_database {

    val aws = AwsConfig().toAwsClient()
    val secretValue = aws.ssmStore["/notion/key"]
    val dbId = "48741c1766314c14938901047680703d"
    val client = OkHttpClient()

    private val log = KotlinLogging.logger {}

    /** XXXX 분할하기 */
    data class NotionColumn(
        val name: String,
        val type: String,
        val value: GsonData,
    ) {

    }

    @Test
    fun `쿼리`() = runBlocking {
        val resp = client.fetch {
            url = "https://api.notion.com/v1/databases/${dbId}/query"
            method = "POST"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
                "Content-Type" to "application/json",
            )
            body = obj {

            }
        }
        if (resp.ok) {
            log.info { "[${resp.response.code}] " }
            val lines = GsonData.parse(resp.respText)["results"].map { line ->
                line["properties"].entryMap().map {
                    val type = it.value["type"].str!!
                    NotionColumn(it.key, type, it.value[type])
                }
            }
            lines.forEach {
                println(it)
            }
        } else {
            log.warn { "[${resp.response.code}] ${resp.respText}" }
        }
    }

    /**
     * https://developers.notion.com/reference/post-page
     * */
    @Test
    fun `입력`() = runBlocking {
        val resp = client.fetch {
            url = "https://api.notion.com/v1/pages"
            method = "POST"
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
                    "이벤트명" to obj {
                        "type" to "title"
                        "title" to arr[
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

        if (resp.ok) {
            log.info { "[${resp.response.code}] " }
            println(resp.respText)
        } else {
            log.warn { "[${resp.response.code}] ${resp.respText}" }
        }
    }


}