package net.kotlinx.csv

import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toInputStream
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.s3
import net.kotlinx.counter.Latch
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.toSiText
import net.kotlinx.okhttp.OkHttpReq
import net.kotlinx.okhttp.download
import net.kotlinx.okhttp.fetchInner
import net.kotlinx.reflect.name
import net.kotlinx.string.CharSets
import net.kotlinx.string.CharUtil
import net.kotlinx.system.ResourceHolder
import net.kotlinx.system.SystemUtil
import okhttp3.OkHttpClient

class CsvReadWriteToolTest : BeSpecLight() {

    private val aws by lazy { koin<AwsClient1>(findProfile97) }

    private val httpClient by koinLazy<OkHttpClient>()

    init {
        initTest(KotestUtil.IGNORE)

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
                    readerFile = file1
                    writerFile = file2
                    writerCharset(CharSets.MS949)
                }
            }

            Then("MS949 로 변경 & zip") {
                CsvReadWriteTool {
                    readerFile = file1
                    writerFile = file3
                    writerCharset(CharSets.MS949)
                    writerGzip = true
                }
            }

            xThen("스트리밍 읽기 & gzip 압축 (메모리 100이내로 충분함)") {
                val s3Data = S3Data.parse("s3://$findProfile97-work-dev/athena/outputLocation/550956bb-3249-4d1f-a970-60819919d4f2.csv")
                aws.s3.getObject(
                    GetObjectRequest {
                        this.bucket = s3Data.bucket
                        this.key = s3Data.key
                    }
                ) {
                    CsvReadWriteTool {
                        readerInputStream = it.body?.toInputStream()!!
                        writerFile = file4
                        writerCharset(CharSets.MS949)
                        writerGzip = true
                    }
                }
                log.info { "파일크기 ${file4.length().toSiText()}" }
                log.info { "사용메모리 ${SystemUtil.nowUsedMemory().toSiText()}" }
            }

            Then("파일정리") {
                workRoot.deleteRecursively()
            }
        }

        Given("TSV 읽고 변환") {

            val host = "https://api.wconcept.co.kr/EP/NAVER/EPUpdate_Naver_V3.txt"
            Then("다운로드") {
                httpClient.download(workRoot.slash("원본파일.tsv")) {
                    url = host
                }
            }

            Then("다운로드 -> 파일처리 -> CSV 변환") {

                val isHeader = Latch()
                val req = OkHttpReq {
                    url = host
                }
                val resp = httpClient.fetchInner(req)
                resp.body.use {
                    CsvReadWriteTool {
                        readerInputStream = it.byteStream()
                        readerFactory = {
                            csvReader {
                                delimiter = '\t' //탭으로 구분 (TSV)
                                escapeChar = CharUtil.USELESS
                                quoteChar = CharUtil.USELESS
                            }
                        }
                        writerFile = workRoot.slash("TSV결과파일.csv")
                        writerFactory = {
                            csvWriter {
                                charset = CharSets.MS949.name()
                            }
                        }
                        processor = { line ->
                            //헤더가 포함된 라인이 넘어옴
                            if (isHeader.check()) {
                                line
                            } else {
                                line.map { "수정됨-$it" }
                            }
                        }
                    }
                }
            }

        }

    }

}
