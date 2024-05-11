package net.kotlinx.aws.athena

import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsConfig
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.toSiText
import net.kotlinx.regex.RegexSet
import net.kotlinx.string.print
import net.kotlinx.string.removeFrom

internal class AthenaModuleTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT01)

        Given("AthenaModule") {

            val awsConfig = koin<AwsConfig>()
            val databaseName = awsConfig.profileName!!.removeFrom(RegexSet.NUMERIC) //주의!
            val athenaModule = AthenaModule(workGroup = "workgroup-prod", database = databaseName)

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

