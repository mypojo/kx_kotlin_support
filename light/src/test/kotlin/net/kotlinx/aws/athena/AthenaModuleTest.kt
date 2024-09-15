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

        }
    }
}

