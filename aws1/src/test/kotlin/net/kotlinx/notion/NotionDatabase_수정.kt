package net.kotlinx.notion

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.ssm.find
import net.kotlinx.aws.toAwsClient1
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test

internal class NotionDatabase_수정 {

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

    @Test
    fun `수정`() = runBlocking {

        val pageId = "04ac0a37-5726-45ae-8eae-f552a4f9a696"
        database.update(pageId)

    }


}