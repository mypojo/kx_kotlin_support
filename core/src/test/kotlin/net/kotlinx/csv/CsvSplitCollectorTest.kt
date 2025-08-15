package net.kotlinx.csv

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import net.kotlinx.file.slash
import net.kotlinx.io.input.toInputResource
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder

internal class CsvSplitCollectorTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("CsvSplitCollector") {

            val workspace = ResourceHolder.WORKSPACE

            Then("헤더 설정 기능 테스트") {
                val customHeaders = listOf("이름", "나이", "도시")
                val testData = listOf(
                    listOf("홍길동", "25", "서울"),
                    listOf("김철수", "30", "부산"),
                    listOf("이영희", "28", "대구")
                )

                val collector = CsvSplitCollector {
                    headers = customHeaders
                    outputStreamFactory = workspace.slash("headerTest").toOutputStreamFactory()
                    //counter.reset(2) // 2개씩 분할하여 헤더가 각 파일에 써지는지 테스트
                }

                // 데이터를 flow로 처리
                listOf(testData).asFlow().collect(collector)
                collector.close()
            }

            Then("헤더 없이 동작 테스트") {
                val testData = listOf(
                    listOf("홍길동", "25", "서울"),
                    listOf("김철수", "30", "부산")
                )

                val collector = CsvSplitCollector {
                    // headers 설정하지 않음
                    outputStreamFactory = workspace.slash("noHeaderTest").toOutputStreamFactory()
                }

                listOf(testData).asFlow().collect(collector)
                collector.close()

                val file = workspace.slash("noHeaderTest").slash("000.csv")
                val lines = file.toInputResource().toFlow().toList().flatten()
                lines.size shouldBe 2
                lines[0] shouldBe testData[0]
                lines[1] shouldBe testData[1]
            }
        }
    }
}