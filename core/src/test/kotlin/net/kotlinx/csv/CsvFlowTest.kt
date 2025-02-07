package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.count
import net.kotlinx.aws.AwsClient
import net.kotlinx.counter.EventCountChecker
import net.kotlinx.file.slash
import net.kotlinx.io.input.toInputResource
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.logback.infos
import net.kotlinx.number.padStart
import net.kotlinx.reflect.name
import net.kotlinx.system.ResourceHolder
import okhttp3.OkHttpClient


class CsvFlowTest : BeSpecLight() {

    private val aws by lazy { Koins.koin<AwsClient>(findProfile97) }

    private val httpClient by Koins.koinLazy<OkHttpClient>()

    init {
        initTest(KotestUtil.IGNORE)

        val workRoot = ResourceHolder.WORKSPACE.slash(this::class.name())
        val file1 = workRoot.slash("테스트파일1.csv")
        val file2 = workRoot.slash("테스트파일2.csv")

        Given("CsvFlow") {

            Then("테스트파일 생성") {
                csvWriter().open(file1) {
                    (0 until 100).forEach { i ->
                        writeRow(listOf("이름$i", "나이$i", "주소$i"))
                    }
                }
                csvWriter().open(file2) {
                    writeRow(listOf("이름", "나이", "주소"))
                    (0 until 100).forEach { i ->
                        writeRow(listOf("이름$i", "나이$i", "주소$i"))
                    }
                }
            }

            When("간단플로우") {
                val flow = file1.toInputResource().toFlow()
                Then("간단 읽기 - 헤더미포함") {
                    flow.chunked(10).collect {
                        log.info { "[${Thread.currentThread().name}] ${it}" }
                    }
                }

                Then("간단 읽기 - 카운트") {
                    log.infos { "카운트 : ${flow.count()}" }
                }

                Then("파일 분할 저장") {
                    CsvSplitCollector {
                        fileFactory = { workRoot.slash("split").slash("${it.padStart(3)}.csv") }
                        counter = EventCountChecker(10)
                    }.use {
                        flow.buffer(3).chunked(3).collect(it)
                    }
                }
            }

        }

    }

}