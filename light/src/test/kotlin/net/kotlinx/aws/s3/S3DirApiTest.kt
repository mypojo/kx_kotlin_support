package net.kotlinx.aws.s3

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight


class S3DirApiTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT)

        Given("S3Data") {

            val dirApi = S3DirApi {
                profile = findProfile97
                bucket = "adpriv-work-dev"
                dirPath = "upload/jobInputFile/"
                csvReader = csvReader()
            }

            Then("그리드 리스팅") {
                val files = dirApi.list()
                files.printSimples()
            }

            Then("파일 다운로드") {
                val link = dirApi.downloadLink("upload/jobInputFile/hpBestAIReviewJobTest.csv")
                log.info { "링크: $link" }
            }

            Then("디렉터토리 전체 파일 스트리밍 처리") {
                dirApi.readAllDirCsvLines().collect {
                    println(it)
                }
            }

        }
    }

}
