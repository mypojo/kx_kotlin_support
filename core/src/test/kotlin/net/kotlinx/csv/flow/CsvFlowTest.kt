package net.kotlinx.csv.flow

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.aws.AwsClient
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.reflect.name
import net.kotlinx.system.ResourceHolder
import okhttp3.OkHttpClient

class CsvFlowTest : BeSpecLight() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    private val httpClient by koinLazy<OkHttpClient>()

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
            Then("간단 읽기") {

                val asd = CsvFlow {
                    readerFile = file1
                }

                asd.collect {
                    println(it)
                }

            }
        }

    }

}
