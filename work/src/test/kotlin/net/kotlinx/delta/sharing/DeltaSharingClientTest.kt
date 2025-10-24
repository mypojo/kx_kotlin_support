package net.kotlinx.delta.sharing

import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import net.kotlinx.csv.CsvCollector
import net.kotlinx.csv.CsvUtil
import net.kotlinx.file.slash
import net.kotlinx.flow.collectClose
import net.kotlinx.io.output.toOutputResource
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.number.toSiText
import net.kotlinx.okhttp.download
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.TimeFormat
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient


class DeltaSharingClientTest : BeSpecHeavy() {

    private val root = ResourceHolder.WORKSPACE.parentFile.parentFile.slash("project").slash("ap").slash("nol")
    private val http by koinLazy<OkHttpClient>()

    init {
        initTest(KotestUtil.IGNORE)

        Given("Delta Sharing 테이블 조회") {

            val config = DeltaSharingConfig {
                secret = root.slash("config.share").readText().toGsonData()
            }

            val client = DeltaSharingClient(config)
            val tables = client.listAllTables()

            Then("모든 Share 목록 조회") {
                tables.printTableGrid()
            }

            Then("테이블 정보 읽기") {
                tables.forEach { table ->
                    log.info { "=== 테이블 읽기 시작: ${table.name()} ===" }

                    val tableFiles = client.getFiles(table)
                    log.info { " -> 스냅샷버전 ${tableFiles.version()}" }

                    val metaData = tableFiles.metaData
                    log.info { " -> 파일 ${metaData["numFiles"].str}건 -> 용량 ${metaData["size"].long!!.toSiText()}" }
                    log.info { " -> 스키마 ${tableFiles.metaDataSchemaFields.map { "${it["name"].str}(${it["type"].str})" }.joinToString(",")}" }

                    val file = tableFiles.file
                    val url = file["url"].str!!
                    val name = url.toHttpUrl().pathSegments.last()
                    log.info { " -> 파일 [${name}] 다운로드 $url" }
                }
            }

            Then("테이블 다운로드 & CSV변환") {
                val basicDate = TimeFormat.YMD.get()
                tables.forEach { table ->
                    val tableFiles = client.getFiles(table)
                    val file = tableFiles.file
                    val url = file["url"].str!!
                    val name = url.toHttpUrl().pathSegments.last()

                    val parquetFile = root.slash("${table.name()}_${basicDate}_${name}")
                    if (!parquetFile.exists()) {
                        log.info { "[${table.name()}] 파일을 다운로드 합니다.. -> ${parquetFile.absolutePath}" }
                        http.download(url, parquetFile)
                    }

                    val parquet = LocalInputFile(parquetFile.toPath())
                    val csvName = "${table.name()}_${basicDate}.csv"

                    if (log.isTraceEnabled) {
                        log.info { "파일 [${csvName}] 샘플링.." }
                        parquet.toFlow().take(3).collect { row -> println(row) }
                    }

                    val csvFile = root.slash(csvName)
                    parquet.toFlow()
                        .map { it.toList() }
                        .chunked(1000)
                        .collectClose {
                            CsvCollector {
                                outputResource = csvFile.toOutputResource()
                                header = tableFiles.metaDataSchemaFields.map { it["name"].str!! }
                                writer = CsvUtil.ms949Writer()
                            }
                        }
                    log.info { "파일 [${csvFile.absolutePath}] 생성완료" }
                }
            }
        }
    }

}
