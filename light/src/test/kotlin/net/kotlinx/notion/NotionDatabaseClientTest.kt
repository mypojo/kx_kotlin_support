package net.kotlinx.notion

import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import java.time.LocalDateTime

internal class NotionDatabaseClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("NotionDatabaseClient") {

            val database by koinLazy<NotionDatabaseClient>()
            val dbId = "3ab409904afb41a88d1530a7879a62c7"

            Then("조회 - 1년 전으로부터 최대 100건") {
                val filter = NotionFilterSet.lastEditAfter(LocalDateTime.now().minusYears(1))
                val notionLines = database.queryAll(dbId, filter)
                notionLines.size shouldBeGreaterThan 0
                notionLines.take(4).print()
            }

            xThen("입력") {
                val data = mapOf(
                    "파일명" to NotionCellBuilder.richText("test.txt"),
                    "처리상태" to NotionCellBuilder.richText("처리중"),
                )
                database.insert(dbId, data)
            }

            xThen("수정") {
                val pageId = "d4be02858e7f4dd4a21e893f64df8ab8"
                database.update(
                    dbId, pageId, listOf(
                        NotionCell2("gceId", NotionCellType.rich_text, "xxxxxxxxxxx"),
                        NotionCell2("별점", NotionCellType.number, "2"),
                    )
                )
            }
        }

    }

}