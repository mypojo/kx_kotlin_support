package net.kotlinx.notion

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.ssm.find
import net.kotlinx.aws.toAwsClient1
import org.junit.jupiter.api.Test

internal class NotionDatabaseClient_입력수정 {

    val aws = AwsConfig().toAwsClient1()
    val secretValue by lazy {
        runBlocking {
            aws.ssm.find("/notion/key")!!
        }
    }
    val dbId = "48741c1766314c14938901047680703d"
    val database = NotionDatabaseClient(secretValue)

    @Test
    fun `수정`() = runBlocking {
        val pageId = "d4be02858e7f4dd4a21e893f64df8ab8"
        database.update(
            dbId, pageId, listOf(
                NotionCell2("gceId", NotionCellType.rich_text, "xxxxxxxxxxx"),
                NotionCell2("별점", NotionCellType.number, "2"),
            )
        )

    }

    @Test
    fun `입력`() = runBlocking {

        val data = mapOf(
            "파일명" to NotionCellBuilder.richText("test.txt"),
            "처리상태" to NotionCellBuilder.richText("처리중"),
        )
        database.insert(dbId,data)

    }


}