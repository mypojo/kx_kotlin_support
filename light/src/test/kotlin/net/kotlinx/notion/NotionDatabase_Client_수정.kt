package net.kotlinx.notion

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.ssm.find
import net.kotlinx.aws.toAwsClient1
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test

internal class NotionDatabase_Client_수정 {

    val aws = AwsConfig().toAwsClient1()
    val secretValue by lazy {
        runBlocking {
            aws.ssm.find("/notion/key")!!
        }
    }
    val dbId = "48741c1766314c14938901047680703d"
    val client = OkHttpClient()
    val database = NotionDatabaseClient(client, secretValue)

    private val log = KotlinLogging.logger {}

    @Test
    fun `수정`() = runBlocking {
        val pageId = "e9921c72-806c-4587-953f-c3c5563af0b4"
        database.update(
            dbId,pageId, listOf(
                NotionCell("gcId", NotionCellType.rich_text, "aabbcc"),
                NotionCell("링크", NotionCellType.url, "https://www.coupang.com/"),
                NotionCell("별점", NotionCellType.number, "1"),
            )
        )

    }


}