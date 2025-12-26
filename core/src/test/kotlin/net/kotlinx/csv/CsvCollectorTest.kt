package net.kotlinx.csv

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.asFlow
import net.kotlinx.file.slash
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.reflect.name
import net.kotlinx.system.ResourceHolder

class CsvCollectorTest : BeSpecLight() {
    init {
        val workRoot = ResourceHolder.WORKSPACE.slash(this::class.name())
        val testFile = workRoot.slash("collector_test.csv")

        Given("CsvCollector") {
            Then("utf8 생성자 테스트") {
                val data = listOf(
                    listOf("v1", "v2"),
                    listOf("v3", "v4")
                )

                CsvCollector.utf8(testFile, listOf("h1", "h2")).use { collector ->
                    listOf(data).asFlow().collect(collector)
                }

                testFile.exists() shouldBe true
                val lines = testFile.readLines()
                lines.size shouldBe 3
                lines[0] shouldBe "h1,h2"
                lines[1] shouldBe "v1,v2"
                lines[2] shouldBe "v3,v4"
            }

            Then("ms949 생성자 테스트") {
                val data = listOf(
                    listOf("한글1", "한글2"),
                    listOf("v3", "v4")
                )

                CsvCollector.ms949(testFile, listOf("헤더1", "헤더2")).use { collector ->
                    listOf(data).asFlow().collect(collector)
                }

                testFile.exists() shouldBe true
                // MS949 인코딩 확인은 복잡하므로 파일 생성 여부와 라인 수 정도만 확인
                val lines = testFile.readLines(charset("MS949"))
                lines.size shouldBe 3
                lines[0] shouldBe "헤더1,헤더2"
                lines[1] shouldBe "한글1,한글2"
            }

            Then("파일 정리") {
                workRoot.deleteRecursively()
            }
        }
    }
}
