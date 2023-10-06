package net.kotlinx.notion

import com.lectra.koson.arr
import com.lectra.koson.obj
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.okhttp.fetch
import net.kotlinx.aws.ssm.find
import net.kotlinx.aws.toAwsClient1
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test

internal class NotionDatabase_등록 {

    val aws = AwsConfig().toAwsClient1()
    val secretValue by lazy {
        runBlocking {
            aws.ssm.find("/notion/key")!!
        }
    }
    val dbId = "48741c1766314c14938901047680703d"
    val client = OkHttpClient()
    val database = NotionDatabase(client, secretValue, dbId)

    private val log = KotlinLogging.logger {}

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