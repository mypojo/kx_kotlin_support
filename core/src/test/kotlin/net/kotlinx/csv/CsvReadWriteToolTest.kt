package net.kotlinx.csv

import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toInputStream
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.toSiText
import net.kotlinx.reflect.name
import net.kotlinx.string.CharSets
import net.kotlinx.system.ResourceHolder
import net.kotlinx.system.SystemUtil

class CsvReadWriteToolTest : BeSpecLight() {

    private val aws by lazy { koin<AwsClient1>(findProfile97) }

    init {
        initTest(KotestUtil.FAST)

        val workRoot = ResourceHolder.WORKSPACE.slash(this::class.name())
        val file1 = workRoot.slash("테스트파일1.csv")
        val file2 = workRoot.slash("테스트파일2.csv")
        val file3 = workRoot.slash("테스트파일3.csv.gz")
        val file4 = workRoot.slash("테스트파일4.csv.gz")

        Given("CsvReadWriteTool") {

            Then("테스트파일 생성") {
                csvWriter().open(file1) {
                    writeRow(listOf("이름", "나이", "주소"))
                    writeRow(listOf("영감님", "87", "개포동"))
                }
            }
            Then("MS949 로 변경") {
                CsvReadWriteTool {
                    inFile = file1
                    outFile = file2
                    outFileCharset = CharSets.MS949
                }
            }

            Then("MS949 로 변경 & zip") {
                CsvReadWriteTool {
                    inFile = file1
                    outFile = file3
                    outFileCharset = CharSets.MS949
                    gzip = true
                }
            }

            Then("스트리밍 읽기 & gzip 압축 (메모리 100이내로 충분함)") {
                val s3Data = S3Data.parse("s3://adpriv-work-dev/athena/outputLocation/550956bb-3249-4d1f-a970-60819919d4f2.csv")
                aws.s3.getObject(
                    GetObjectRequest {
                        this.bucket = s3Data.bucket
                        this.key = s3Data.key
                    }
                ) {
                    CsvReadWriteTool {
                        inInputStream = it.body?.toInputStream()!!
                        outFile = file4
                        outFileCharset = CharSets.MS949
                        gzip = true
                    }
                }
                log.info { "파일크기 ${file4.length().toSiText()}" }
                log.info { "사용메모리 ${SystemUtil.nowUsedMemory().toSiText()}" }
            }

            Then("파일정리") {
                workRoot.deleteRecursively()
            }
        }
    }

}
