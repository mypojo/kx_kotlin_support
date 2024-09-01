package net.kotlinx.knotion

import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.toTextGridPrint
import org.jraf.klibnotion.client.NotionClient


class KnotionSupportKtTest : BeSpecLight() {

    private val client by koinLazy<NotionClient>()

    init {
        initTest(KotestUtil.FAST)

        Given("데이터베이스") {

            val dbId = "3ab409904afb41a88d1530a7879a62c7"

            Then("조회") {
                val resps = client.databases.queryDatabase(dbId)

                listOf("ID", "등록", "수정", "날짜", "이벤트명", "내용상세").toTextGridPrint {
                    resps.results.map { page ->
                        arrayOf(
                            page.id,
                            page.created.toText(),
                            page.lastEdited.toText(),
                            page.propertyValues.firstOrNull { it.name == "날짜" }?.toValueString(),
                            page.propertyValues.firstOrNull { it.name == "이벤트명" }?.toValueString(),
                            page.propertyValues.firstOrNull { it.name == "내용상세" }?.toValueString(),
                        )
                    }
                }
            }
        }
    }

}
