package net.kotlinx.notion

import com.lectra.koson.obj
import io.kotest.matchers.ints.shouldBeGreaterThan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import net.kotlinx.string.toTextGridPrint

internal class NotionDatabaseClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("NotionDatabaseClient") {

            val database by koinLazy<NotionDatabaseClient>()
            val dbId = "3ab409904afb41a88d1530a7879a62c7"

            Then("일반조회") {
                val pages = database.query(dbId).first()
                pages.size shouldBeGreaterThan 0
                listOf("id", "created", "edited", "내용상세").toTextGridPrint {
                    pages.map {
                        val elements = it.properties
                        arrayOf(it.id, it.createdTime, it.lastEditedTime, elements["이벤트명"])
                    }
                }
            }

            Then("조회 - 1년 전으로부터 최대 100건") {
                //val filter = NotionFilterSet.lastEditAfter(LocalDateTime.now().minusYears(1))
                val filter = null
                val pages = database.query(dbId, filter).toList().flatten()
                pages.size shouldBeGreaterThan 0
                pages.take(4).print()
            }

            Then("특정 타겟 조회") {
                //val filter = NotionFilterSet.lastEditAfter(LocalDateTime.now().minusYears(1))
                val filter = null
                val pages = database.query(dbId, filter).toList().flatten()
                pages.size shouldBeGreaterThan 0
                pages.take(4).print()
            }

            //==================================================== 이하 수벙 ======================================================

            Then("신규 입력") {
                val data = obj {
                    "파일명" to NotionCell.RichText.toNotion("test.txt")
                    "처리상태" to NotionCell.RichText.toNotion("처리중")
                }
                database.insert(dbId, data)
            }

            Then("속성 수정") {
                val pageId = "d4be02858e7f4dd4a21e893f64df8ab8"
                val properties = obj {
                    // rich_text 예시
                    "gceId" to NotionCell.RichText.toNotion("xxxxxxxxxxx")
                    // number 예시
                    "별점" to NotionCell.NumberCell.toNotion("2")
                }
                database.update(dbId, pageId, properties)
            }

            Then("삭제") {
                val pageId = "d4be02858e7f4dd4a21e893f64df8ab8"
                database.delete(pageId)
            }
        }

    }

}