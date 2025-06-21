package net.kotlinx.aws.s3

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import net.kotlinx.aws.AwsClient
import net.kotlinx.csv.CsvUtil
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight


class S3DirApiTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT)

        Given("S3Data") {

            val dirApi = S3DirApi {
                client = koin<AwsClient>()
                bucket = "demo.kotlinx.net"
                dirPath = "S3DirApi/"
                csvReader = CsvUtil.ms949Reader()
            }

            Then("그리드 리스팅") {
                val files = dirApi.list()
                files.printSimples()
            }

            Then("파일 다운로드") {
                val link = dirApi.downloadLink("S3DirApi/kwd_N001.csv")
                log.info { "링크: $link" }
            }

            Then("디렉터토리 전체 파일 스트리밍 처리 - 일반읽기") {
                val lines = dirApi.readAllDirCsvLines().merge().toList()
                lines.forEachIndexed { i, it ->
                    log.info { " -> 전체 데이터 $i ${it}" }
                }
            }

            Then("디렉터토리 전체 파일 스트리밍 처리 - 헤더 제거 & 첫 로우만 누적 (중복제거)") {
                //헤더 제거 & 첫 로우만 누적 (중복제거)
                val unique = mutableSetOf<String>()
                dirApi.readAllDirCsvLines().map {
                    it.drop(1) //헤더를 드랍
                }.merge().collect {
                    unique += it[0] //첫 로우를 누적
                }
                unique.forEachIndexed { i, it ->
                    log.info { " -> 전체 데이터 $i ${it}" }
                }
            }

        }
    }

}
