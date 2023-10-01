package net.kotlinx.aws

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.okhttp.fetch
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test

internal class Notion_테스트 {

    val aws = AwsConfig().toAwsClient()
    val secretValue = aws.ssmStore["/notion/key"]

    private val log = KotlinLogging.logger {}

    @Test
    fun `페에지 읽기`() = runBlocking {

        val pageId = "94b11ef1a332476283b77b54fb19f12d"

        val client = OkHttpClient()

        val resp = client.fetch {
            url = "https://api.notion.com/v1/pages/${pageId}"
            header = mapOf(
                "Authorization" to "Bearer $secretValue",
                "Notion-Version" to "2022-06-28",
            )
        }
        if (resp.ok) {
            log.info { "[${resp.response.code}] ${resp.respText}" }
        } else {
            log.warn { "[${resp.response.code}] ${resp.respText}" }
        }


    }


}