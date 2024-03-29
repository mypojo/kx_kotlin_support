package net.kotlinx.notion

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.ssm.find
import net.kotlinx.aws.toAwsClient1

internal class NotionDatabaseClient_읽기 {

    val aws = AwsConfig().toAwsClient1()
    val secretValue by lazy {
        runBlocking {
            aws.ssm.find("/notion/key")!!
        }
    }
    val dbId = "48741c1766314c14938901047680703d"
    val database = NotionDatabaseClient(secretValue)

    private val log = KotlinLogging.logger {}

//    val sorter: (NotionRow) -> String? = { it.colimns[1].value } //시작일 기준으로 정렬
//
//    @Test
//    fun `쿼리`() = runBlocking {
//        val filter = NotionFilterSet.lastEditAfter(LocalDateTime.now().minusHours(3))
//        val notionLine = database.queryAll(dbId, filter)
//        val headers = notionLine.first().colimns.map { it.name } + listOf("id", "생성", "수정")
//        val datas = notionLine.sortedBy(sorter).map { line ->
//            (line.colimns.map { it.value } + listOf(line.id, line.createdTime.toKr01(), line.lastEditedTime.toKr01())).toTypedArray()
//        }
//        headers.toTextGrid(datas).print()
//    }



}