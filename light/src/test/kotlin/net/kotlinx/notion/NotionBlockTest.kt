package net.kotlinx.notion

import io.kotest.matchers.shouldBe
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.modules.BeSpecHeavy

class NotionBlockTest : BeSpecHeavy() {

    init {
        Given("NotionBlock 마크다운 변환 테스트") {

            When("heading_2 블록인 경우 (페이지 제목용)") {
                val json = """
                    {
                      "id": "title-1",
                      "type": "heading_2",
                      "heading_2": {
                        "rich_text": [
                          { "type": "text", "plain_text": "테스트 페이지 제목" }
                        ]
                      }
                    }
                """.trimIndent()
                val block = NotionBlock(GsonData.parse(json))

                Then("## 테스트 페이지 제목 이 반환되어야 함") {
                    block.markdown shouldBe "## 테스트 페이지 제목"
                }
            }

            When("heading_3 블록인 경우") {
                val json = """
                    {
                      "id": "block-1",
                      "type": "heading_3",
                      "heading_3": {
                        "rich_text": [
                          { "type": "text", "text": { "content": "목적/배경" }, "plain_text": "목적/배경" }
                        ]
                      }
                    }
                """.trimIndent()
                val block = NotionBlock(GsonData.parse(json))

                Then("### 목적/배경 이 반환되어야 함") {
                    block.markdown shouldBe "### 목적/배경"
                }
            }

            When("numbered_list_item 블록인 경우") {
                val json = """
                    {
                      "id": "block-2",
                      "type": "numbered_list_item",
                      "numbered_list_item": {
                        "rich_text": [
                          { "type": "text", "text": { "content": "사내 솔루션 전체에 대해서 보안 조치가 필요함" }, "plain_text": "사내 솔루션 전체에 대해서 보안 조치가 필요함" }
                        ]
                      }
                    }
                """.trimIndent()
                val block = NotionBlock(GsonData.parse(json), depth = 0)

                Then("1. ... 이 반환되어야 함") {
                    block.markdown shouldBe "1. 사내 솔루션 전체에 대해서 보안 조치가 필요함"
                }

                Then("depth가 1이면 들여쓰기가 포함되어야 함") {
                    val blockWithDepth = NotionBlock(GsonData.parse(json), depth = 1)
                    blockWithDepth.markdown shouldBe "  1. 사내 솔루션 전체에 대해서 보안 조치가 필요함"
                }
            }

            When("table_row 블록인 경우 (헤더)") {
                val json = """
                    {
                      "id": "block-3",
                      "type": "table_row",
                      "table_row": {
                        "cells": [
                          [{ "type": "text", "text": { "content": "내용" }, "plain_text": "내용" }],
                          [{ "type": "text", "text": { "content": "예상공수" }, "plain_text": "예상공수" }],
                          [{ "type": "text", "text": { "content": "설명" }, "plain_text": "설명" }],
                          [{ "type": "text", "text": { "content": "실공수" }, "plain_text": "실공수" }]
                        ]
                      }
                    }
                """.trimIndent()
                val block = NotionBlock(GsonData.parse(json), isTableHeader = true)

                Then("마크다운 테이블 행과 구분선이 반환되어야 함") {
                    block.markdown shouldBe "| 내용 | 예상공수 | 설명 | 실공수 |\n| --- | --- | --- | --- |"
                }
            }

            When("table_row 블록에 개행이 포함된 경우") {
                val json = """
                    {
                      "id": "block-4",
                      "type": "table_row",
                      "table_row": {
                        "cells": [
                          [{ "type": "text", "text": { "content": "1.\n2." }, "plain_text": "1.\n2." }]
                        ]
                      }
                    }
                """.trimIndent()
                val block = NotionBlock(GsonData.parse(json))

                Then("개행이 <br>로 치환되어야 함") {
                    block.markdown shouldBe "| 1.<br>2. |"
                }
            }
        }

        Given("NotionBlock 리스트 번호 매기기 테스트") {
            val json = { id: String, text: String ->
                """
                {
                  "id": "$id",
                  "type": "numbered_list_item",
                  "numbered_list_item": {
                    "rich_text": [ { "type": "text", "text": { "content": "$text" }, "plain_text": "$text" } ]
                  }
                }
                """.trimIndent()
            }

            val blocks = listOf(
                NotionBlock(GsonData.parse(json("1", "첫번째"))),
                NotionBlock(GsonData.parse(json("2", "두번째"))),
                NotionBlock(GsonData.parse(json("3", "세번째")))
            )

            When("applyListOrders를 실행하면") {
                val orderedBlocks = blocks.applyListOrders()

                Then("번호가 1, 2, 3으로 순차적으로 매겨져야 함") {
                    orderedBlocks[0].markdown shouldBe "1. 첫번째"
                    orderedBlocks[1].markdown shouldBe "2. 두번째"
                    orderedBlocks[2].markdown shouldBe "3. 세번째"
                }
            }

            When("중간에 다른 블록이 있으면") {
                val dividerJson = """{ "id": "d1", "type": "divider", "divider": {} }"""
                val blocksWithDivider = listOf(
                    NotionBlock(GsonData.parse(json("1", "첫번째"))),
                    NotionBlock(GsonData.parse(dividerJson)),
                    NotionBlock(GsonData.parse(json("2", "다시 첫번째")))
                )
                val orderedBlocks = blocksWithDivider.applyListOrders()

                Then("번호가 초기화되어야 함") {
                    orderedBlocks[0].markdown shouldBe "1. 첫번째"
                    orderedBlocks[1].markdown shouldBe "---"
                    orderedBlocks[2].markdown shouldBe "1. 다시 첫번째"
                }
            }
        }

        Given("NotionBlock 테이블 헤더 자동 설정 테스트") {
            val json = { id: String, content: String ->
                """
                {
                  "id": "$id",
                  "type": "table_row",
                  "table_row": {
                    "cells": [ [{ "type": "text", "text": { "content": "$content" }, "plain_text": "$content" }] ]
                  }
                }
                """.trimIndent()
            }

            val blocks = listOf(
                NotionBlock(GsonData.parse(json("r1", "헤더"))),
                NotionBlock(GsonData.parse(json("r2", "데이터")))
            )

            When("applyListOrders를 실행하면") {
                val processedBlocks = blocks.applyListOrders()

                Then("첫 번째 행만 헤더 구분선이 포함되어야 함") {
                    processedBlocks[0].markdown shouldBe "| 헤더 |\n| --- |"
                    processedBlocks[1].markdown shouldBe "| 데이터 |"
                }
            }
        }
    }
}
