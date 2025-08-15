package net.kotlinx.csv

import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.flowOf
import net.kotlinx.file.slash
import net.kotlinx.flow.collectClose
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.reflect.name
import net.kotlinx.system.ResourceHolder
import java.io.File

class CsvNamedSplitCollectorTest : BeSpecLog() {

    init {
        initTest(KotestUtil.PROJECT)

        Given("CsvNamedSplitCollector") {

            val workRoot = ResourceHolder.WORKSPACE.slash(CsvNamedSplitCollectorTest::class.name())

            When("separator 함수로 파일 분할") {

                Then("간단 테스트") {
                    val flow = flowOf(
                        listOf("user1", "typeA", "data1", "value1"),
                        listOf("user2", "typeB", "data2", "value2"),
                        listOf("user3", "typeA", "data3", "value3"),
                        listOf("user4", "typeB", "data4", "value4")
                    )

                    flow.chunked(3).collectClose {
                        CsvNamedSplitCollector {
                            separator = { it[1] } // typeA, typeB로 분할
                            outputStreamFactory = workRoot.toOutputStreamFactory()
                        }
                    }

                    val typeAFile = File(workRoot, "typeA.csv")
                    val typeBFile = File(workRoot, "typeB.csv")

                    log.info { "typeA.csv 존재: ${typeAFile.exists()}" }
                    log.info { "typeB.csv 존재: ${typeBFile.exists()}" }
                }

                xThen("대용량 테스트") {
                    val flow = flowOf(
                        *(1..10000).map { index ->
                            val typeList = listOf("A", "B", "C", "D", "E", "F", "G")
                            val type = typeList[index % typeList.size]
                            listOf("user$index", "type$type", "data$index", "value$index")
                        }.toTypedArray()
                    )

                    flow.chunked(100).collectClose {
                        CsvNamedSplitCollector {
                            separator = { it[1] }
                            outputStreamFactory = workRoot.toOutputStreamFactory()
                        }
                    }

                    val typeFiles = listOf("A", "B", "C", "D", "E", "F", "G").map { type -> File(workRoot, "type$type.csv") }
                    typeFiles.forEach { file ->
                        log.info { "${file.name} 존재: ${file.exists()}, 크기: ${if (file.exists()) file.length() else 0} bytes" }

                    }
                }

            }

            When("헤더 설정 기능") {

                Then("헤더가 각 분할 파일에 자동 추가") {
                    val customHeaders = listOf("사용자", "타입", "데이터", "값")
                    val flow = flowOf(
                        listOf("user1", "typeA", "data1", "value1"),
                        listOf("user2", "typeB", "data2", "value2"),
                        listOf("user3", "typeA", "data3", "value3"),
                        listOf("user4", "typeB", "data4", "value4")
                    )

                    val headerTestRoot = workRoot.slash("headerTest")

                    flow.chunked(3).collectClose {
                        CsvNamedSplitCollector {
                            separator = { it[1] } // typeA, typeB로 분할
                            outputStreamFactory = headerTestRoot.toOutputStreamFactory()
                            headers = customHeaders
                        }
                    }

                    val typeAFile = File(headerTestRoot, "typeA.csv")
                    val typeBFile = File(headerTestRoot, "typeB.csv")

                    log.info { "헤더 테스트 - typeA.csv 존재: ${typeAFile.exists()}" }
                    log.info { "헤더 테스트 - typeB.csv 존재: ${typeBFile.exists()}" }
                }

                Then("헤더 없이도 정상 동작") {
                    val flow = flowOf(
                        listOf("user1", "typeC", "data1", "value1"),
                        listOf("user2", "typeD", "data2", "value2")
                    )

                    val noHeaderTestRoot = workRoot.slash("noHeaderTest")

                    flow.chunked(3).collectClose {
                        CsvNamedSplitCollector {
                            separator = { it[1] }
                            outputStreamFactory = noHeaderTestRoot.toOutputStreamFactory()
                            // headers 설정하지 않음
                        }
                    }

                    val typeCFile = File(noHeaderTestRoot, "typeC.csv")
                    val typeDFile = File(noHeaderTestRoot, "typeD.csv")

                    log.info { "헤더 없음 테스트 - typeC.csv 존재: ${typeCFile.exists()}" }
                    log.info { "헤더 없음 테스트 - typeD.csv 존재: ${typeDFile.exists()}" }
                }

            }
        }
    }
}