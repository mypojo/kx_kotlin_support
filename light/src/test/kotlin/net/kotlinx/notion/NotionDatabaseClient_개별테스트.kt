package net.kotlinx.notion

import io.kotest.matchers.ints.shouldBeGreaterThan
import kotlinx.coroutines.flow.first
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoadStringSsm
import net.kotlinx.string.toTextGridPrint

internal class NotionDatabaseClient_개별테스트 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("NotionDatabaseClient") {

            val dbId = "2b6effe4827d80fe8b08f8a1419cbc5a"
            val key by lazyLoadStringSsm("/secret/api/notion", findProfile49)
            val database = NotionDatabaseClient(key)

            Then("DB 리스트 조회") {
                val pages = database.query(dbId).first()
                pages.size shouldBeGreaterThan 0
                listOf("id", "created", "edited", "내용상세").toTextGridPrint {
                    pages.map {
                        val elements = it.properties
                        arrayOf(it.id, it.createdTime, it.lastEditedTime, elements["이벤트명"])
                    }
                }
            }

            val pageId = "2b6effe4-827d-80a6-bc63-c7afc5708d06"
            val pageClient = NotionPageClient(key)
            Then("DB의 단일 페이지 조회") {
                val list = pageClient.list(pageId).first()
                list.forEach { println(it.contents) }
            }

            val blockClient = NotionBlockClient(key)
            Then("블록 칠드런 조회") {
                val blockId = "2bdeffe4-827d-80a5-9588-e7f9e03368b8"
                val list = blockClient.list(blockId).first()
                list.forEach { println(it.contents) }
            }

            Then("전체로더 테스트") {
                val loader = NotionPageLoader(key)
                val list = loader.load(pageId)
                list.forEach {
                    println(it.markdown)
                }
            }

            Then("표 업데이트") {
                val rowId = "2bdeffe4-827d-80ea-b86e-e0a56e7e99e5" // 첫 데이터 로우(UI 구성...)
                blockClient.updateTableRow(rowId) { list ->
                    list.toMutableList().apply {
                        this[2] = this[2] + "."
                        this[3] = "${this[3].toLong() + 1}"
                    }
                }
            }


        }

    }

}