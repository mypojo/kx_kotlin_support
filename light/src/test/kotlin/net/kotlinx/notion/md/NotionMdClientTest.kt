package net.kotlinx.notion.md

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.string.shouldNotBeEmpty
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoadStringSsm

internal class NotionMdClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("NotionMdClient") {

            // 테스트용 페이지 ID (NotionDatabaseClient_개별테스트에서 사용한 페이지)
            val pageId = "2b6effe4-827d-80a6-bc63-c7afc5708d06"
            val key by lazyLoadStringSsm("/secret/api/notion", findProfile49)
            val client = NotionMdClient(key)

            Then("페이지를 마크다운으로 변환 (재귀)") {
                val markdown = client.toMarkdown(pageId, recursive = true, depthLevel = 3)

                markdown.shouldNotBeEmpty()
                markdown.length shouldBeGreaterThan 10

                println("=== 변환된 마크다운 (재귀) ===")
                println(markdown)
                println("=== 마크다운 길이: ${markdown.length} ===")
            }

            Then("페이지를 마크다운으로 변환 (비재귀)") {
                val markdown = client.toMarkdown(pageId, recursive = false)

                markdown.shouldNotBeEmpty()

                println("=== 변환된 마크다운 (비재귀) ===")
                println(markdown)
                println("=== 마크다운 길이: ${markdown.length} ===")
            }

            Then("여러 페이지를 순차적으로 변환") {
                val pageIds = listOf(
                    "2b6effe4-827d-80a6-bc63-c7afc5708d06", // 테스트 페이지
                )

                pageIds.forEach { id ->
                    val markdown = client.toMarkdown(id, recursive = true, depthLevel = 2)
                    markdown.shouldNotBeEmpty()
                    println("페이지 $id 변환 완료: ${markdown.length} 문자")
                }
            }

        }

    }

}
