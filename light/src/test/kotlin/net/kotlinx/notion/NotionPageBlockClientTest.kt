package net.kotlinx.notion

import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.toTextGrid
import net.kotlinx.time.TimeFormat

class NotionPageBlockClientTest : BeSpecLight() {

    init {
        initTest(KotestUtil.SLOW)

        Given("NotionPageBlockClient") {
            val page by koinLazy<NotionPageBlockClient>()
            val pageId = "4b18e3f52ce84487b64acab8ab2b5837"

            Then("블록조회") {
                val blocks = page.blocks(pageId, 10)
                log.info { "블록사이즈 ${blocks.size}" }
                listOf("id", "block type", "cell type", "viewText").toTextGrid(blocks.map { arrayOf(it.id, it.type, it.cell?.type ?: "", it.cell?.viewText ?: "") }).print()
            }

            xThen("블록수정") {
                page.updateParagraph("77d5dc38-fdb8-4da5-9b6c-7a097b1e130e") {
                    "rich_text" to NotionCellBuilder.richText("변경텍스트_${TimeFormat.YMDHMSS_K01.get()}")
                }
            }


        }
    }

}