package net.kotlinx.aws.athena

import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.number.toSiText
import net.kotlinx.string.print

internal class AthenaModuleTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient1>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("AthenaModule") {

            val athenaModule = AthenaModule {
                aws = koin<AwsClient1>(findProfile97)
                workGroup = "workgroup-dev"
                database = ""
            }

            Then("쿼리 정상출력 & 다운로드 동시실행됨") {
                val query = "SELECT * FROM conv limit 10"
                athenaModule.startAndWait(
                    listOf(
                        AthenaReadAll(query) {
                            log.info { "쿼리를 즉시 읽어옴" }
                            it.print()
                        },
                        AthenaDownload(query) { file ->
                            log.info { "파일 다운로드됨 -> ${file.absolutePath} / ${file.length().toSiText()}" }
                            file.delete() shouldBe true
                        }
                    )
                )
            }

//            Then("다운로드 & 파일 변환 & 프리사인") {
////                val exe = athenaModule.execute("SELECT * FROM nv_site_data limit 3")
////                val location = exe.outputLocation
//                val location = "s3://adpriv-work-dev/athena/outputLocation/37299e75-ed53-4a88-84e4-d80ce7be2329.csv"
//                val s3Data = S3Data.parse(location)
//                val fileUtf8 = ResourceHolder.WORKSPACE.slash("AthenaModuleTest_sin_meta").slash("text_utf8.csv")
//                fileUtf8.parentFile.mkdirs()
//                if (!fileUtf8.exists()) {
//                    aws.s3.getObjectDownload(s3Data.bucket, s3Data.key, fileUtf8)
//                }
//                fileUtf8.writeCsvToCharset(fileUtf8.nameAppend("_MS949"))
//            }


        }
    }
}

