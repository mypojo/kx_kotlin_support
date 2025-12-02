package net.kotlinx.notion

import com.lectra.koson.arr
import com.lectra.koson.obj
import kotlinx.coroutines.flow.first
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.TimeFormat

class NotionPageBlockClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("NotionPageBlockClient") {

            val page by koinLazy<NotionBlockClient>()
            val pageId = "23b20bec-25ea-441f-8de6-448e9c8a5e95"

            Then("블록조회") {
                val blocks = page.list(pageId, 10).first()
                log.info { "블록사이즈 ${blocks.size}" }
                listOf("id", "block type", "cell type", "viewText").toTextGridPrint {
                    blocks.map { arrayOf(it.id, it.type, it.cell?.type ?: "", it.cell?.viewText ?: "") }
                }
            }

            xThen("블록수정") {
                page.updateParagraph("77d5dc38-fdb8-4da5-9b6c-7a097b1e130e") {
                    "rich_text" to NotionCell.RichText.toNotion("변경텍스트_${TimeFormat.YMDHMSS_K01.get()}")
                }
            }

            Then("블록추가 ") {
                page.insertTableRow(pageId, obj {
                    "children" to arr[
                        obj {
                            "object" to "block"
                            "type" to "paragraph"
                            "paragraph" to obj {
                                "rich_text" to arr[
                                    obj {
                                        "type" to "text"
                                        "text" to obj {
                                            "content" to "추가텍스트_${TimeFormat.YMDHMSS_K01.get()}"
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                })
            }

            Then("블록추가 리스트") {
                page.insertTableRow(pageId, obj {
                    "children" to arr[
                        obj {
                            "object" to "block"
                            "type" to "numbered_list_item"
                            "numbered_list_item" to obj {
                                "rich_text" to arr[
                                    obj {
                                        "type" to "text"
                                        "text" to obj { "content" to "A작업" }
                                    }
                                ]
                            }
                        },
                        obj {
                            "object" to "block"
                            "type" to "numbered_list_item"
                            "numbered_list_item" to obj {
                                "rich_text" to arr[
                                    obj {
                                        "type" to "text"
                                        "text" to obj { "content" to "B작업 진행중" }
                                    }
                                ]
                            }
                        }
                    ]
                })
            }


        }
    }

}